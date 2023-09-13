package kr.co.hs.sudoku

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.impl.LocaleEntityImpl
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl
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

@Suppress("NonAsciiCharacters")
@OptIn(ExperimentalCoroutinesApi::class)
class BattleTestCase1 {
    private lateinit var battleRepository2: BattleRepositoryImpl
    private lateinit var ownerUser: ProfileEntity
    private lateinit var guestUser1: ProfileEntity
    private lateinit var guestUser2: ProfileEntity
    private lateinit var matrix: IntMatrix
    private lateinit var battleEntity: BattleEntity

    @Before
    fun init() = runTest {
        battleRepository2 = BattleRepositoryImpl()
        ownerUser = ProfileEntityImpl(
            "owner",
            "ownerUser",
            "this is owner",
            "http://owner.com",
            LocaleEntityImpl("ko", "KR")
        )
        guestUser1 = ProfileEntityImpl(
            "guest1",
            "guestUser1",
            "this is first guest",
            "http://",
            LocaleEntityImpl("ko", "KR")
        )
        guestUser2 = ProfileEntityImpl(
            "guest2",
            "guestUser2",
            "this is second guest",
            "http://",
            LocaleEntityImpl("ko", "KR")
        )
        matrix = CustomMatrix(
            listOf(
                listOf(1, 4, 3, 2),
                listOf(2, 3, 1, 4),
                listOf(4, 1, 2, 3),
                listOf(3, 2, 4, 0)
            )
        )
        battleEntity = battleRepository2.createBattle(ownerUser, matrix, 3)
    }

    @Test
    fun 참여자_없이_강제로_시작() = runTest {
        assertThrows(Exception::class.java) {
            runBlocking { battleRepository2.startBattle(battleEntity, ownerUser.uid) }
        }
    }

    @Test
    fun 참여자는_있지만_아직_ready_안한_상태에서_시작() = runTest {
        battleRepository2.joinBattle(battleEntity, guestUser1)

        assertThrows(Exception::class.java) {
            runBlocking { battleRepository2.startBattle(battleEntity, ownerUser.uid) }
        }

        battleRepository2.exitBattle(battleEntity, guestUser1)
    }

    @Test
    fun 방_검색() = runTest {
        val list = battleRepository2.getBattleList(10)
        val selectEntity = list.find { it.id == battleEntity.id }
        assertNotNull(selectEntity)
    }

    @Test
    fun 참여중인_방_검색() = runTest {
        val ownerJoinedBattle = battleRepository2.getJoinedBattle(ownerUser.uid)
        assertEquals(ownerJoinedBattle?.id, battleEntity.id)

        val joinedBattle = battleRepository2.getJoinedBattle(guestUser1.uid)
        assertNotEquals(joinedBattle?.id, battleEntity.id)

        val invalidJoinedBattle = battleRepository2.getJoinedBattle("없는 uid")
        assertNull(invalidJoinedBattle)
    }

    @Test
    fun 준비상태_확인_테스트() = runTest {
        battleRepository2.joinBattle(battleEntity, guestUser1)
        battleRepository2.joinBattle(battleEntity, guestUser2)

        assertFalse(battleRepository2.isAllReady(battleEntity))

        battleRepository2.readyToBattle(guestUser1.uid)

        assertFalse(battleRepository2.isAllReady(battleEntity))

        battleRepository2.exitBattle(battleEntity, guestUser2)

        assertTrue(battleRepository2.isAllReady(battleEntity))
    }

    @Test
    fun host가_바뀌는_상황() = runTest {
        battleRepository2.joinBattle(battleEntity, guestUser1)
        battleRepository2.joinBattle(battleEntity, guestUser2)

        var battle = battleRepository2.getBattle(battleEntity.id)
        assertNotNull(battle)
        assertEquals(ownerUser.uid, battle?.host)

        battleRepository2.exitBattle(battleEntity, ownerUser)
        battle = battleRepository2.getBattle(battleEntity.id)
        assertNotNull(battle)
        assertNotEquals(ownerUser.uid, battle?.host)
    }

    @Test
    fun 게임이_시작된_방에_참여_하려_하는_경우() = runTest {
        battleRepository2.joinBattle(battleEntity, guestUser1)
        battleRepository2.joinBattle(battleEntity, guestUser2)

        assertThrows(Exception::class.java) {
            runTest { battleRepository2.startBattle(battleEntity, ownerUser.uid) }
        }

        battleRepository2.readyToBattle(guestUser1.uid)

        assertThrows(Exception::class.java) {
            runTest { battleRepository2.startBattle(battleEntity, ownerUser.uid) }
        }

        battleRepository2.readyToBattle(guestUser2.uid)

        battleRepository2.pendingBattle(battleEntity, ownerUser.uid)
        battleRepository2.startBattle(battleEntity, ownerUser.uid)

        val list = battleRepository2.getBattleList(10)
        val current = list.find { it.id == battleEntity.id }
        // 참여 가능한 battleEntity가 존재
        assertNull(current)

        // 이 상태에서 참여
        assertThrows(Exception::class.java) {
            val thirdparty = ProfileEntityImpl(
                "thirdparty",
                "thirdparty",
                "thirdparty",
                "http://thirdparty.com",
                LocaleEntityImpl("ko", "KR")
            )
            runBlocking { battleRepository2.joinBattle(battleEntity, thirdparty) }
        }

        battleRepository2.exitBattle(battleEntity, guestUser1)
        battleRepository2.exitBattle(battleEntity, guestUser2)
    }

    @Test
    fun 게임_클리어_하였을때_기록() = runTest {
        battleRepository2.joinBattle(battleEntity, guestUser1)
        battleRepository2.readyToBattle(guestUser1.uid)

        battleRepository2.joinBattle(battleEntity, guestUser2)
        battleRepository2.readyToBattle(guestUser2.uid)

        battleRepository2.pendingBattle(battleEntity, ownerUser.uid)
        battleRepository2.startBattle(battleEntity, ownerUser.uid)

        val runningBattle = battleRepository2.getBattle(battleEntity.id)!!

        battleRepository2.updateClearRecord(runningBattle, guestUser1, 1000)
        battleRepository2.updateClearRecord(runningBattle, ownerUser, 500)
        battleRepository2.updateClearRecord(runningBattle, guestUser2, 1500)

        val ownerUserStatistics = battleRepository2.getStatistics(ownerUser.uid)
        assertTrue(ownerUserStatistics.winCount > 0)

        val guestUserStatistics = battleRepository2.getStatistics(guestUser1.uid)
        assertTrue(guestUserStatistics.clearedCount > 0)
    }

    @Test
    fun pending_없이_시작하려고_하는_경우() = runTest {
        battleRepository2.joinBattle(battleEntity, guestUser1)
        battleRepository2.readyToBattle(guestUser1.uid)

        battleRepository2.joinBattle(battleEntity, guestUser2)
        battleRepository2.readyToBattle(guestUser2.uid)

        assertThrows(Exception::class.java) {
            runTest { battleRepository2.startBattle(battleEntity, ownerUser.uid) }
        }

        battleRepository2.pendingBattle(battleEntity, ownerUser.uid)
        battleRepository2.startBattle(battleEntity, ownerUser.uid)

        val running = battleRepository2.getBattle(battleEntity.id)

        assertTrue(running is BattleEntity.RunningBattleEntity)
    }

    @After
    fun cleared() = runTest {
        battleRepository2.exitBattle(battleEntity, ownerUser)
        val list = battleRepository2.getParticipantList(battleEntity.id)
        list.forEach {
            battleRepository2.exitBattle(battleEntity, it)
        }
    }
}