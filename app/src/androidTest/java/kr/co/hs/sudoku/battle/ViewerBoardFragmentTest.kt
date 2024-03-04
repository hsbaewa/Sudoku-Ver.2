package kr.co.hs.sudoku.battle

import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kr.co.hs.sudoku.HiltTestUtil
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.ViewMatchers.checkCellValue
import kr.co.hs.sudoku.feature.stage.StageFragment
import kr.co.hs.sudoku.feature.multi.play.MultiPlayViewerStageFragment
import kr.co.hs.sudoku.model.battle.ParticipantEntity
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("TestFunctionName", "NonAsciiCharacters")
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ViewerBoardFragmentTest {

    private val testStartingMatrix = listOf(
        listOf(1, 0, 0, 1),
        listOf(0, 1, 1, 0),
        listOf(1, 0, 0, 1),
        listOf(0, 1, 1, 0)
    )
    private val generatedMatrix = listOf(
        testStartingMatrix[0].map { if (it > 0) 2 else 0 },
        testStartingMatrix[1].map { if (it > 0) 2 else 0 },
        testStartingMatrix[2].map { if (it > 0) 2 else 0 },
        testStartingMatrix[3].map { if (it > 0) 2 else 0 }
    )

    private lateinit var fragmentScenario: HiltTestUtil.HiltFragmentScenario<MultiPlayViewerStageFragment>

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun initFragmentScenario() {
        hiltRule.inject()
        fragmentScenario = HiltTestUtil.launchFragmentInHiltContainer(
            fragmentArgs = StageFragment.newInstanceArguments(CustomMatrix(testStartingMatrix)),
            themeResId = R.style.Theme_HSSudoku2,
            initialState = Lifecycle.State.RESUMED
        )
    }

    private val host = ParticipantEntity.Host("host's uid", "my name is host")
    private val guest = ParticipantEntity.Guest("guest's uid", "my name is guest")
    private val ready = ParticipantEntity.ReadyGuest(guest.uid, guest.displayName)
    private val playing = ParticipantEntity.Playing(
        uid = guest.uid,
        displayName = guest.displayName,
        matrix = CustomMatrix(generatedMatrix)
    )

    @Test
    fun 참여자_뷰어_UI_테스트() {
        fragmentScenario.onFragment {
            it.setStatus(guest)
        }


        fragmentScenario.onFragment {
            it.setStatus(playing)
        }

        fragmentScenario.onFragment {
            val customMatrix = CustomMatrix(playing.matrix)
            customMatrix[0, 1] = 1
            it.setStatus(
                ParticipantEntity.Playing(
                    guest.uid,
                    guest.displayName,
                    matrix = customMatrix
                )
            )
        }

    }

    @Test
    fun 방장_뷰어_UI_테스트() {
        fragmentScenario.onFragment {
            it.setValues(generatedMatrix)
            it.setStatus(host)
        }

        onView(withId(R.id.viewSilhouette)).check(matches(isDisplayed()))
        onView(withText("Host")).check(matches(isDisplayed()))

        fragmentScenario.onFragment {
            it.setStatus(guest)
        }
        onView(withId(R.id.viewSilhouette)).check(matches(isDisplayed()))
        onView(withText("Waiting")).check(matches(isDisplayed()))

        fragmentScenario.onFragment {
            it.setStatus(ready)
        }
        onView(withId(R.id.viewSilhouette)).check(matches(isDisplayed()))
        onView(withText("is Ready")).check(matches(isDisplayed()))

        fragmentScenario.onFragment {
            it.setStatus(
                ParticipantEntity.Playing(
                    "uid", "name", matrix = CustomMatrix(
                        listOf(
                            listOf(0, 1, 0, 0),
                            listOf(0, 0, 0, 0),
                            listOf(0, 0, 0, 0),
                            listOf(0, 0, 0, 0)
                        )
                    )
                )
            )
        }
        onView(withId(R.id.viewSilhouette)).check(matches(not(isDisplayed())))
        onView(withText("is Ready")).check(doesNotExist())


        fragmentScenario.onFragment {
            it.setStatus(
                ParticipantEntity.Playing(
                    "uid", "name", matrix = CustomMatrix(
                        listOf(
                            listOf(2, 1, 3, 0),
                            listOf(0, 0, 0, 0),
                            listOf(0, 0, 0, 0),
                            listOf(0, 0, 0, 0)
                        )
                    )
                )
            )
        }
        runBlocking { delay(500) }
        onView(withId(R.id.viewSilhouette)).check(matches(not(isDisplayed())))
        onView(withId(R.id.sudoku_view)).check(matches(checkCellValue(0, 2, 3)))
    }
}