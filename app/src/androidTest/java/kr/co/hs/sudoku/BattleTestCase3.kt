package kr.co.hs.sudoku

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.impl.LocaleEntityImpl
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl
import kr.co.hs.sudoku.repository.battle.BattleRepositoryImpl
import kr.co.hs.sudoku.usecase.battle.BattleMatrixControlUseCase
import kr.co.hs.sudoku.usecase.battle.BattleMonitorUseCase
import kr.co.hs.sudoku.usecase.battle.ParticipantMonitorUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@Suppress("NonAsciiCharacters")
@OptIn(ExperimentalCoroutinesApi::class)
class BattleTestCase3 {

    private lateinit var battleRepository2: BattleRepositoryImpl
    private lateinit var user1: ProfileEntity
    private lateinit var user2: ProfileEntity
    private lateinit var matrix: IntMatrix
    private lateinit var battleEntity: BattleEntity

    @Before
    fun init() = runTest {
        battleRepository2 = BattleRepositoryImpl()
        user1 = ProfileEntityImpl(
            "user1",
            "user1",
            "this is user1",
            "http://user1.com",
            LocaleEntityImpl("ko", "kr")
        )
        user2 = ProfileEntityImpl(
            "user2",
            "user2",
            "this is user2",
            "http://user2",
            LocaleEntityImpl("ko", "kr")
        )
        matrix = CustomMatrix(
            listOf(
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )
        battleEntity = battleRepository2.createBattle(user1, matrix)
        battleRepository2.joinBattle(battleEntity, user2)
        battleRepository2.readyToBattle(user2.uid)
    }

    @Test
    fun 방_참가자_모니터링() = runBlocking {
        val useCase = ParticipantMonitorUseCase(battleRepository2)
        var lastMatrix: List<List<Int>>? = null
        val monitorJob = launch {
            useCase(battleEntity, user1.uid).collect {
                lastMatrix = it.matrix
            }
        }

        delay(1000)
        val controlUseCase = BattleMatrixControlUseCase(battleRepository2, user1.uid)
        controlUseCase(0, 0, 1)
        delay(1000)
        assertEquals(1, lastMatrix?.get(0)?.get(0))

        controlUseCase(0, 1, 2)
        delay(1000)
        assertEquals(2, lastMatrix?.get(0)?.get(1))

        monitorJob.cancel()
    }

    @Test
    fun 게임_상태_모니터() = runBlocking {
        val useCase = BattleMonitorUseCase(battleRepository2)
        var winnerUid: String? = null
        val monitorJob = launch {
            useCase(battleEntity).collect {
                if (it is BattleEntity.ClearedBattleEntity) {
                    winnerUid = it.winner
                }
            }
        }

        assertTrue(battleRepository2.isAllReady(battleEntity))

        battleRepository2.pendingBattle(battleEntity, user1.uid)
        battleRepository2.startBattle(battleEntity, user1.uid)

        val startingBattle = battleRepository2.getBattle(battleId = battleEntity.id)!!
        assertTrue(startingBattle is BattleEntity.RunningBattleEntity)

        delay(1000)
        battleRepository2.updateClearRecord(startingBattle, user1, 4800)
        battleRepository2.updateClearRecord(startingBattle, user2, 4900)

        delay(1000)
        assertEquals(user1.uid, winnerUid)

        monitorJob.cancel()
    }

    @After
    fun cleared() = runTest {
        battleRepository2.exitBattle(battleEntity, user1)
        battleRepository2.exitBattle(battleEntity, user2)
    }
}