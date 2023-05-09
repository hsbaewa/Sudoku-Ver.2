package kr.co.hs.sudoku

import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BattleTestCase2 {
    private lateinit var battleRepository2: BattleRepositoryImpl
    private lateinit var ownerUser: ProfileEntity
    private lateinit var matrix: IntMatrix
    private lateinit var battleEntitySet: Set<BattleEntity>


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
        matrix = CustomMatrix(
            listOf(
                listOf(1, 4, 3, 2),
                listOf(2, 3, 1, 4),
                listOf(4, 1, 2, 3),
                listOf(3, 2, 4, 0)
            )
        )
        val set = HashSet<BattleEntity>()
        repeat(10) {
            set.add(battleRepository2.createBattle(ownerUser, matrix))
        }
        battleEntitySet = set
    }

    @Test
    fun get_battle_for_paging() = runTest {
        val page1 = battleRepository2.getBattleList(3)
        assertEquals(3, page1.size)

        val page2 = battleRepository2.getBattleList(3, page1.last().createdAt)
        assertEquals(true, page1.last().createdAt > page2.first().createdAt)

        val page3 = battleRepository2.getBattleList(3, page2.last().createdAt)
        assertEquals(true, page2.last().createdAt > page3.first().createdAt)

        val page4 = battleRepository2.getBattleList(3, page3.last().createdAt)
        assertEquals(1, page4.size)
        assertEquals(true, page3.last().createdAt > page4.first().createdAt)
    }

    @After
    fun cleared() = runTest {
        battleEntitySet.forEach {
            battleRepository2.exitBattle(it, ownerUser)
        }
    }
}