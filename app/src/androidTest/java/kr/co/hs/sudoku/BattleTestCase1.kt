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
            LocaleEntityImpl("ko", "kr")
        )
        guestUser1 = ProfileEntityImpl(
            "guest1",
            "guestUser1",
            "this is first guest",
            "http://",
            LocaleEntityImpl("ko", "kr")
        )
        guestUser2 = ProfileEntityImpl(
            "guest2",
            "guestUser2",
            "this is second guest",
            "http://",
            LocaleEntityImpl("ko", "kr")
        )
        matrix = CustomMatrix(
            listOf(
                listOf(1, 4, 3, 2),
                listOf(2, 3, 1, 4),
                listOf(4, 1, 2, 3),
                listOf(3, 2, 4, 0)
            )
        )
        battleEntity = battleRepository2.createBattle(ownerUser, matrix)
    }

    @Test
    fun force_Start_Battle() = runTest {
        assertThrows(Exception::class.java) {
            runBlocking { battleRepository2.startBattle(battleEntity, ownerUser.uid) }
        }
    }

    @Test
    fun force_Start_Battle_Not_Ready() = runTest {
        battleRepository2.joinBattle(battleEntity, guestUser1)

        assertThrows(Exception::class.java) {
            runBlocking { battleRepository2.startBattle(battleEntity, ownerUser.uid) }
        }

        battleRepository2.exitBattle(battleEntity, guestUser1)
    }

    @Test
    fun search_battle() = runTest {
        val list = battleRepository2.getBattleList(10)
        val selectEntity = list.find { it.id == battleEntity.id }
        assertNotNull(selectEntity)
    }

    @Test
    fun search_joined_battle() = runTest {
        val ownerJoinedBattle = battleRepository2.getJoinedBattle(ownerUser)
        assertEquals(ownerJoinedBattle?.id, battleEntity.id)

        val joinedBattle = battleRepository2.getJoinedBattle(guestUser1)
        assertNotEquals(joinedBattle?.id, battleEntity.id)
    }

    @Test
    fun ready_to_battle() = runTest {
        battleRepository2.joinBattle(battleEntity, guestUser1)
        battleRepository2.joinBattle(battleEntity, guestUser2)

        assertFalse(battleRepository2.isAllReady(battleEntity))

        battleRepository2.readyToBattle(guestUser1)

        assertFalse(battleRepository2.isAllReady(battleEntity))

        battleRepository2.exitBattle(battleEntity, guestUser2)

        assertTrue(battleRepository2.isAllReady(battleEntity))
    }

    @Test
    fun changed_host() = runTest {
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
    fun start_battle_and_eject_not_clear() = runTest {
        battleRepository2.joinBattle(battleEntity, guestUser1)
        battleRepository2.joinBattle(battleEntity, guestUser2)

        assertThrows(Exception::class.java) {
            runTest { battleRepository2.startBattle(battleEntity, ownerUser.uid) }
        }

        battleRepository2.readyToBattle(guestUser1)

        assertThrows(Exception::class.java) {
            runTest { battleRepository2.startBattle(battleEntity, ownerUser.uid) }
        }

        battleRepository2.readyToBattle(guestUser2)

        battleRepository2.startBattle(battleEntity, ownerUser.uid)

        val list = battleRepository2.getBattleList(10)
        val current = list.find { it.id == battleEntity.id }
        assertNull(current)

        battleRepository2.exitBattle(battleEntity, guestUser1)
        battleRepository2.exitBattle(battleEntity, guestUser2)
    }

    @Test
    fun start_battle_and_clear_sequence() = runTest {
        battleRepository2.joinBattle(battleEntity, guestUser1)
        battleRepository2.readyToBattle(guestUser1)

        battleRepository2.joinBattle(battleEntity, guestUser2)
        battleRepository2.readyToBattle(guestUser2)

        battleRepository2.startBattle(battleEntity, ownerUser.uid)

        battleRepository2.updateClearRecord(battleEntity, guestUser1, 1000)
        battleRepository2.updateClearRecord(battleEntity, ownerUser, 500)
        battleRepository2.updateClearRecord(battleEntity, guestUser2, 1500)

        val ownerUserStatistics = battleRepository2.getStatistics(ownerUser.uid)
        assertTrue(ownerUserStatistics.winCount > 0)

        val guestUserStatistics = battleRepository2.getStatistics(guestUser1.uid)
        assertTrue(guestUserStatistics.clearedCount > 0)
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