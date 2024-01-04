package kr.co.hs.sudoku

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.battle.ParticipantEntity
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.BeginnerMatrixRepository
import kr.co.hs.sudoku.repository.ProfileRepositoryImpl
import kr.co.hs.sudoku.repository.battle.BattleEventRepositoryImpl
import kr.co.hs.sudoku.repository.battle.BattleRepository
import kr.co.hs.sudoku.repository.battle.BattleRepositoryImpl
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration

@Suppress("NonAsciiCharacters", "TestFunctionName", "SpellCheckingInspection")
@OptIn(ExperimentalCoroutinesApi::class)
open class BattleRepositoryTest {
    private val _userProfile = ArrayList<ProfileEntity>()
    private val _userBattleRepository = ArrayList<BattleRepository>()

    open val userUidList = listOf(
        "OZVSz3lIRnPBK5hbmkhc0icyFfB3",
        "gVZdnB5mKbWoYuE7HbbNZ9pp7Fi1",
        "lYYBEGzX9JggNlChJs7C9OPtVe82",
        "zDPKLMyyhTNCU4uKp2KDb0Kc3Py1"
    )

    @Before
    open fun initRepository() = runTest {
        val profileRepository = ProfileRepositoryImpl()

        userUidList.forEach {
            _userProfile.add(profileRepository.getProfile(it))
            val battleRepository = spyk<BattleRepositoryImpl>(recordPrivateCalls = true)
            every { battleRepository.getProperty("currentUserUid") } returns it
            _userBattleRepository.add(battleRepository)
        }
    }

    @After
    open fun releaseRepository() = runTest {
        userBattleRepository
            .forEach {
                it.runCatching { exit() }.getOrNull()
            }
    }

    private suspend fun getTestMatrix() = with(BeginnerMatrixRepository()) {
        FirebaseRemoteConfig.getInstance().fetchAndActivate().await()
        getList().first()
    }

    protected val userProfile: List<ProfileEntity> by ::_userProfile
    protected val userBattleRepository: List<BattleRepository> by ::_userBattleRepository

    @Test
    fun 게임_생성_테스트() = runTest(timeout = Duration.INFINITE) {
        var battle = userBattleRepository[0].create(getTestMatrix())
        assertEquals(battle.host, userUidList[0])
        assertEquals(2, battle.maxParticipants)


        assertThrows(Exception::class.java) {
            runBlocking {
                userBattleRepository[0].create(getTestMatrix())
            }
        }.also {
            assertEquals(
                it.message,
                "이미 생성된 게임(${battle.id})이 있습니다. 생성된 게임을 종료 후 다시 시도 해 주세요."
            )
        }


        battle = userBattleRepository[1].create(getTestMatrix(), 3)
        assertEquals(battle.host, userUidList[1])
        assertEquals(3, battle.maxParticipants)


        /*
        이벤트 검증
         */
        val eventRepository = BattleEventRepositoryImpl(battle.id)
        eventRepository.startMonitoring()

        launch {
            eventRepository.battleFlow.collect {

                val event = it
                assertTrue(event is BattleEntity.Opened)

                assertEquals(battle, event)

                if (event.participants.size == 1) {
                    userBattleRepository[1].getParticipants(battle)
                    assertEquals(battle.participants.first(), event.participants.first())
                    this.cancel()
                }
            }
        }.invokeOnCompletion {
            eventRepository.stopMonitoring()
        }
    }

    @Test
    open fun 게임_참여_테스트() = runTest(timeout = Duration.INFINITE) {
        var battle = userBattleRepository[0].create(getTestMatrix(), 3)
        assertEquals(1, battle.participantSize)
        assertEquals(userUidList[0], battle.host)

        /*
        이벤트 검증
         */
        val eventRepository = BattleEventRepositoryImpl(battle.id)
        eventRepository.startMonitoring()

        launch {
            eventRepository.battleFlow.collect {

                val event = it
                assertTrue(event is BattleEntity.Opened)

                when (event.participants.size) {
                    1 -> {
                        assertNotNull(event.participants.find { it.uid == userProfile[0].uid })
                        assertNull(event.participants.find { it.uid == userProfile[1].uid })
                        assertNull(event.participants.find { it.uid == userProfile[3].uid })
                    }

                    2 -> {
                        assertNotNull(event.participants.find { it.uid == userProfile[0].uid })
                        assertNotNull(event.participants.find { it.uid == userProfile[1].uid })
                        assertNull(event.participants.find { it.uid == userProfile[3].uid })
                    }

                    3 -> {
                        assertNotNull(event.participants.find { it.uid == userProfile[0].uid })
                        assertNotNull(event.participants.find { it.uid == userProfile[1].uid })
                        assertNotNull(event.participants.find { it.uid == userProfile[3].uid })
                        this.cancel()
                    }
                }
            }
        }.invokeOnCompletion {
            eventRepository.stopMonitoring()
        }

        assertThrows(Exception::class.java) {
            battle =
                runBlocking { with(userBattleRepository[0]) { join(battle.id);getParticipating() } }
        }.also { assertEquals(it.message, "참여 하려는 게임(${battle.id})에 이미 참가 중 입니다.") }
        assertEquals(1, battle.participantSize)
        assertEquals(userUidList[0], battle.host)


        battle = with(userBattleRepository[1]) { join(battle.id);getParticipating() }
        assertEquals(2, battle.participantSize)
        assertEquals(userUidList[0], battle.host)


        assertThrows(Exception::class.java) {
            battle =
                runBlocking { with(userBattleRepository[1]) { join(battle.id);getParticipating() } }
        }.also { assertEquals(it.message, "참여 하려는 게임(${battle.id})에 이미 참가 중 입니다.") }
        assertEquals(2, battle.participantSize)
        assertEquals(userUidList[0], battle.host)

        assertEquals(
            userBattleRepository[0].getParticipating(),
            userBattleRepository[1].getParticipating()
        )

        userBattleRepository[2].create(getTestMatrix())
        assertNotEquals(
            userBattleRepository[0].getParticipating(),
            userBattleRepository[2].getParticipating()
        )

        // 참가 중인 게임이 있는 경우 무시하고 다른 게임에 참여 하도록 변경 함.
//        assertThrows(Exception::class.java) {
//            runBlocking { userBattleRepository[2].join(battle.id) }
//        }.also { assertTrue(it.message?.endsWith("참가 중인 게임을 종료 후 다시 시도 해 주세요.") ?: false) }

        assertThrows(Exception::class.java) {
            runBlocking { userBattleRepository[3].join("any") }
        }.also { assertEquals(it.message, "유효 하지 않은 게임(any)입니다.") }

        battle = with(userBattleRepository[3]) { join(battle.id);getParticipating() }
        assertEquals(3, battle.participantSize)
        assertEquals(userUidList[0], battle.host)

        userBattleRepository[2].exit()
        assertThrows(Exception::class.java) {
            runBlocking { userBattleRepository[2].join(battle.id) }
        }.also {
            assertEquals(
                it.message,
                "게임(${battle.id})의 참여자가 ${battle.participantSize}/${battle.maxParticipants}로 이미 가득 찼습니다."
            )
        }
    }

    @Test
    fun 게임_찾기_테스트() = runTest(timeout = Duration.INFINITE) {
        assertThrows(Exception::class.java) {
            runBlocking { userBattleRepository[0].getParticipating() }
        }.also { assertEquals(it.message, "참여중인 게임이 없습니다.") }
        assertFalse(userBattleRepository[0].isParticipating())

        with(userBattleRepository[0]) {
            assertEquals(create(getTestMatrix()), getParticipating())
            list().run {
                assertEquals(true, isNotEmpty())
                assertTrue(contains(getParticipating()))
            }
        }

        with(userBattleRepository[1]) {
            assertEquals(create(getTestMatrix()), getParticipating())
            list().run {
                assertEquals(true, size >= 2)
                assertTrue(contains(getParticipating()))
            }
        }

        with(userBattleRepository[2]) {
            search(userBattleRepository[0].getParticipating().id)
                .run { assertEquals(userBattleRepository[0].getParticipating(), this) }
        }
    }

    @Test
    fun 게임_준비_테스트() = runTest(timeout = Duration.INFINITE) {
        assertThrows(Exception::class.java) {
            runBlocking { userBattleRepository[0].ready() }
        }.also { assertEquals(it.message, "현재 어떠한 방에도 참여 중이지 않습니다.") }

        val battle = userBattleRepository[0].create(getTestMatrix())
        assertThrows(Exception::class.java) {
            runBlocking { userBattleRepository[0].ready() }
        }.also { assertEquals(it.message, "이미 준비 상태가 true 상태입니다.") }

        val eventRepository = BattleEventRepositoryImpl(battleId = battle.id)
        eventRepository.startMonitoring()

        launch {
            eventRepository.battleFlow.collect {
                assertEquals(battle.id, it.id)

                when (it.participants.size) {
                    1 -> {
                        assertNotNull(it.participants.find { it.uid == userProfile[0].uid })
                        assertNull(it.participants.find { it.uid == userProfile[1].uid })
                    }

                    2 -> {
                        assertNotNull(it.participants.find { it.uid == userProfile[0].uid })
                        assertNotNull(it.participants.find { it.uid == userProfile[1].uid })
                    }
                }

                it.participants.find { it.uid == userProfile[1].uid }
                    ?.run {
                        if (this is ParticipantEntity.ReadyGuest) {
                            cancel()
                        }
                    }
            }
        }.invokeOnCompletion {
            eventRepository.stopMonitoring()
        }

        assertThrows(Exception::class.java) {
            runBlocking { userBattleRepository[0].unready() }
        }.also { assertEquals(it.message, "host(${battle.host})는 ready 상태 변경이 불가 합니다.") }

        userBattleRepository[1].join(battle.id)
        userBattleRepository[1].ready()

        userBattleRepository[1].unready()
    }

    @Test
    open fun 게임_시작_테스트() = runTest(timeout = Duration.INFINITE) {
        var battle = userBattleRepository[0].create(getTestMatrix())


        val eventRepository = BattleEventRepositoryImpl(battleId = battle.id)
        eventRepository.startMonitoring()

        launch {
            eventRepository.battleFlow.collect {
                assertEquals(battle.id, it.id)

                if (it is BattleEntity.Playing) {
                    if (it.participants.none { it !is ParticipantEntity.Playing }) {
                        cancel()
                    }
                }
            }
        }.invokeOnCompletion { eventRepository.stopMonitoring() }


        assertThrows(Exception::class.java) {
            runBlocking { userBattleRepository[0].start() }
        }.also { assertEquals(it.message, "방이 게임을 시작 할 수 있는 상태가 아닙니다. 먼저 pending을 호출하세요.") }

        userBattleRepository[1].join(battle.id)

        assertThrows(Exception::class.java) {
            runBlocking { userBattleRepository[0].pendingStart() }
        }.also { assertEquals(it.message, "아직 모든 참여자가 준비가 되어 있지 않습니다.") }

        userBattleRepository[1].ready()

        assertThrows(Exception::class.java) {
            runBlocking { userBattleRepository[1].pendingStart() }
        }.also { assertEquals(it.message, "오직 방장(${battle.host})만이 게임을 시작 할 수 있습니다.") }

        assertTrue(battle is BattleEntity.Opened)
        userBattleRepository[0].pendingStart()
        battle = userBattleRepository[0].getParticipating()
        assertTrue(battle is BattleEntity.Pending)

        userBattleRepository[0].getParticipants(battle)
        battle.participants.forEach { entity ->
            assertTrue(entity is ParticipantEntity.ReadyGuest || entity is ParticipantEntity.Host)
//            val matrix = CustomMatrix((entity as ParticipantEntity.ReadyGuest).matrix)
//            assertEquals(matrix.boxCount, 2)
//            assertEquals(matrix.boxSize, 2)
//            assertEquals(matrix.size, 4)
//            assertTrue(
//                matrix.flatten().find { it > 1 } != null)
        }

        assertTrue(battle is BattleEntity.Pending)
        userBattleRepository[0].start()
        battle = userBattleRepository[0].getParticipating()
        assertTrue(battle is BattleEntity.Playing)

        userBattleRepository[0].getParticipants(battle)
        battle.participants.forEach { entity ->
            assertTrue(entity is ParticipantEntity.Playing)
            val matrix = CustomMatrix((entity as ParticipantEntity.Playing).matrix)
            assertEquals(matrix.boxCount, 2)
            assertEquals(matrix.boxSize, 2)
            assertEquals(matrix.size, 4)
            assertTrue(
                matrix.flatten().find { it > 1 } != null)
        }
    }


    @Test
    open fun 게임_종료_테스트() = runTest(timeout = Duration.INFINITE) {
        var battleId: String?
        assertEquals(
            userBattleRepository[0].create(getTestMatrix()).apply {
                battleId = this.id
            },
            userBattleRepository[0].getParticipating()
        )

        var eventRepository = BattleEventRepositoryImpl(battleId = battleId!!)
        eventRepository.startMonitoring()

        launch {
            eventRepository.battleFlow.collect {
                when (it) {
                    is BattleEntity.Invalid -> cancel()
                    else -> assertEquals(battleId, it.id)
                }
                println(it)
            }
        }.invokeOnCompletion { eventRepository.stopMonitoring() }

        userBattleRepository[0].exit()
        assertFalse(userBattleRepository[0].isParticipating())

        var battle = userBattleRepository[0].create(getTestMatrix())

        eventRepository = BattleEventRepositoryImpl(battleId = battle.id)
        eventRepository.startMonitoring()

        launch {
            eventRepository.battleFlow.collect {
                when (it) {
                    is BattleEntity.Invalid -> cancel()
                    else -> {
                        assertEquals(battle.id, it.id)

                        when (it.participants.size) {
                            1 -> {
//                                assertNotNull(it.participants.find { it.uid == userProfile[0].uid })
//                                assertNull(it.participants.find { it.uid == userProfile[1].uid })
                            }

                            2 -> {
                                assertNotNull(it.participants.find { it.uid == userProfile[0].uid })
                                assertNotNull(it.participants.find { it.uid == userProfile[1].uid })
                            }
                        }
                    }
                }
                println(it)

                if (it.host != userProfile[0].uid && it.participants.find { it.uid == userProfile[1].uid } is ParticipantEntity.Host) {
                    cancel()
                }
            }
        }.invokeOnCompletion { eventRepository.stopMonitoring() }

        userBattleRepository[1].join(battle.id)

        userBattleRepository[1].ready()

        userBattleRepository[0].exit()

        battle = userBattleRepository[1].getParticipating()

        assertEquals(battle.host, userProfile[1].uid)
    }

    @Test
    open fun 게임_클리어_테스트() = runTest(timeout = Duration.INFINITE) {
        var battle = userBattleRepository[0].create(getTestMatrix())

        val eventRepository = BattleEventRepositoryImpl(battleId = battle.id)
        eventRepository.startMonitoring()

        launch {
            var hasAllReady = false
            var hasPending = false
            var hasStarted = false

            eventRepository.battleFlow.collect {
                assertEquals(battle.id, it.id)

//                if (it is BattleEntity2.Playing) {
//                    if (it.participants.none { it !is ParticipantEntity.Playing }) {
//                        cancel()
//                    }
//                }
                when (it.participants.size) {
                    1 -> {
                        assertNotNull(it.participants.find { it.uid == userProfile[0].uid })
                        assertNull(it.participants.find { it.uid == userProfile[1].uid })
                    }

                    2 -> {
                        assertNotNull(it.participants.find { it.uid == userProfile[0].uid })
                        assertNotNull(it.participants.find { it.uid == userProfile[1].uid })
                    }
                }

                if (!hasAllReady && it.participants.filter { it is ParticipantEntity.Guest }
                        .isEmpty()) {
                    hasAllReady = true
                }

                if (!hasPending && it is BattleEntity.Pending) {
                    hasPending = true
                }

                if (!hasStarted && it is BattleEntity.Playing) {
                    hasStarted = true
                }

                val clearedParticipant = it.participants.filter { it is ParticipantEntity.Cleared }
                if (clearedParticipant.size == 2 && hasAllReady && hasPending && hasStarted) {
                    cancel()
                }

            }
        }.invokeOnCompletion { eventRepository.stopMonitoring() }

        assertThrows(Exception::class.java) {
            runBlocking { userBattleRepository[0].clear(1000) }
        }.also {
            assertEquals(
                it.message,
                "아직 게임(${battle.id})이 시작 되지 않았습니다. 게임이 시작 된 이후에 기록을 저장 할 수 있습니다."
            )
        }

        userBattleRepository[1].join(battle.id)

        assertThrows(Exception::class.java) {
            runBlocking { userBattleRepository[1].clear(1000) }
        }.also {
            assertEquals(
                it.message,
                "아직 게임(${battle.id})이 시작 되지 않았습니다. 게임이 시작 된 이후에 기록을 저장 할 수 있습니다."
            )
        }

        userBattleRepository[1].ready()
        assertThrows(Exception::class.java) {
            runBlocking { userBattleRepository[1].clear(1000) }
        }.also {
            assertEquals(
                it.message,
                "아직 게임(${battle.id})이 시작 되지 않았습니다. 게임이 시작 된 이후에 기록을 저장 할 수 있습니다."
            )
        }

        userBattleRepository[0].pendingStart()
        assertThrows(Exception::class.java) {
            runBlocking { userBattleRepository[1].clear(1000) }
        }.also {
            assertEquals(
                it.message,
                "아직 게임(${battle.id})이 시작 되지 않았습니다. 게임이 시작 된 이후에 기록을 저장 할 수 있습니다."
            )
        }

        userBattleRepository[0].start()
        userBattleRepository[1].clear(1000)
        assertThrows(Exception::class.java) {
            runBlocking { userBattleRepository[1].clear(1000) }
        }.also { assertEquals(it.message, "이미 1000ms로 클리어 한 기록이 있습니다.") }

        assertFalse(userBattleRepository[1].isParticipating())

        battle = userBattleRepository[0].getParticipating()
        userBattleRepository[0].getParticipants(battle)

        assertEquals(1, battle.participants.filterIsInstance<ParticipantEntity.Cleared>().size)
        assertEquals(
            1000,
            battle.participants.filterIsInstance<ParticipantEntity.Cleared>().first().record
        )
        assertEquals(1, battle.participants.filterIsInstance<ParticipantEntity.Playing>().size)

        userBattleRepository[0].clear(2000)
    }

    @Test
    fun 클리어_기록_조회_테스트() = runTest(timeout = Duration.INFINITE) {
        var battle = userBattleRepository[0].create(getTestMatrix())

        val eventRepository = BattleEventRepositoryImpl(battleId = battle.id)
        eventRepository.startMonitoring()

        launch {
            eventRepository.battleFlow.collect {

                if (it.participants.size == 1 && (it.participants.first() as? ParticipantEntity.Cleared)?.record == 1000L) {
                    cancel()
                }

            }
        }.invokeOnCompletion {
            eventRepository.stopMonitoring()
        }

        userBattleRepository[1].join(battle.id)
        userBattleRepository[1].ready()

        userBattleRepository[0].pendingStart()
        userBattleRepository[0].start()

        userBattleRepository[0].clear(1000)

        var statistics = userBattleRepository[0].getStatistics()
        assertTrue(statistics.winCount > 0)

        userBattleRepository[1].exit()

        battle = userBattleRepository[0].create(getTestMatrix())

        val eventRepository2 = BattleEventRepositoryImpl(battleId = battle.id)
        eventRepository2.startMonitoring()

        launch {
            eventRepository2.battleFlow.collect {

                if (
                    it.participants.find {
                        it is ParticipantEntity.Cleared
                                && it.record == 3000L
                                && it.uid == userProfile[0].uid
                    } != null &&
                    it.participants.find {
                        it is ParticipantEntity.Cleared
                                && it.record == 2000L
                                && it.uid == userProfile[1].uid
                    } != null
                ) {
                    cancel()
                }
            }
        }.invokeOnCompletion {
            eventRepository2.stopMonitoring()
        }

        userBattleRepository[1].join(battle.id)
        userBattleRepository[1].ready()

        userBattleRepository[0].pendingStart()
        userBattleRepository[0].start()

        coroutineScope {
            launch {
                userBattleRepository[1].clear(2000)
            }
            launch {
                userBattleRepository[0].clear(3000)
            }
        }


        statistics = userBattleRepository[0].getStatistics()
        assertTrue(statistics.winCount > 0)
        assertTrue(statistics.winCount <= statistics.clearedCount)
    }

    @Test
    open fun 셀_변경_테스트() = runTest(timeout = Duration.INFINITE) {
        val battle = userBattleRepository[0].create(getTestMatrix())

        val eventRepository2 = BattleEventRepositoryImpl(battleId = battle.id)
        eventRepository2.startMonitoring()

        launch {
            eventRepository2.battleFlow.collect {
                if (it is BattleEntity.Playing) {
                    it.participants.find { it.uid == userProfile[1].uid }
                        ?.run {
                            if ((this as ParticipantEntity.Playing).matrix[0][1] == 3) {
                                cancel()
                            }
                        }
                }
            }
        }.invokeOnCompletion {
            eventRepository2.stopMonitoring()
        }

        userBattleRepository[1].join(battle.id)
        userBattleRepository[1].ready()

        userBattleRepository[0].pendingStart()
        userBattleRepository[0].start()


        userBattleRepository[1].updateMatrix(0, 1, 3)

    }
}