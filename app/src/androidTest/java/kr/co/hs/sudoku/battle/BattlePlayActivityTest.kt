package kr.co.hs.sudoku.battle

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.feature.battle2.BattlePlayActivity
import kr.co.hs.sudoku.model.battle2.BattleEntity
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.repository.battle2.BattleRepositoryImpl
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@Suppress("TestFunctionName", "NonAsciiCharacters", "SpellCheckingInspection")
@RunWith(AndroidJUnit4::class)
class BattlePlayActivityTest {

    private val testStartingMatrix = listOf(
        listOf(1, 0, 0, 1),
        listOf(0, 1, 1, 0),
        listOf(1, 0, 0, 1),
        listOf(0, 1, 1, 0)
    )

    private lateinit var createdBattle: BattleEntity

    @Before
    fun createBattle() = runTest {
        val battleRepository = spyk<BattleRepositoryImpl>(recordPrivateCalls = true)
        every { battleRepository.getProperty("currentUserUid") } returns "zDPKLMyyhTNCU4uKp2KDb0Kc3Py1"
        createdBattle = battleRepository.create( CustomMatrix(testStartingMatrix))
    }


    @After
    fun exitBattle() = runTest {
        val battleRepository = spyk<BattleRepositoryImpl>(recordPrivateCalls = true)
        every { battleRepository.getProperty("currentUserUid") } returns "zDPKLMyyhTNCU4uKp2KDb0Kc3Py1"
        battleRepository.exit()
    }

    @Test
    fun 시작() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), BattlePlayActivity::class.java)
        intent.putExtra("kr.co.hs.sudoku.EXTRA_BATTLE_ID", createdBattle.id)

        val activityScenarioRule = ActivityScenario.launch<BattlePlayActivity>(intent)

        activityScenarioRule.onActivity {
            mockkObject(it, recordPrivateCalls = true)
            every { it.getProperty("currentUserUid") } returns "zDPKLMyyhTNCU4uKp2KDb0Kc3Py1"
        }

        activityScenarioRule.moveToState(Lifecycle.State.RESUMED)

        onView(withText("Start Game")).check(matches(isDisplayed()))

    }
}