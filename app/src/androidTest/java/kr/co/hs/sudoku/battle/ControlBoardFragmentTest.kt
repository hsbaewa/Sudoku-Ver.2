package kr.co.hs.sudoku.battle

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
import kr.co.hs.sudoku.feature.stage.StageFragment
import kr.co.hs.sudoku.feature.multi.play.MultiPlayControlStageFragment
import kr.co.hs.sudoku.model.battle.ParticipantEntity
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("TestFunctionName", "NonAsciiCharacters", "SpellCheckingInspection")
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ControlBoardFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun before() {
        hiltRule.inject()
    }

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

    private lateinit var fragmentScenario: HiltTestUtil.HiltFragmentScenario<MultiPlayControlStageFragment>

    @Before
    fun initFragmentScenario() {
        fragmentScenario = HiltTestUtil.launchFragmentInHiltContainer(
            fragmentArgs = StageFragment.newInstanceArguments(CustomMatrix(testStartingMatrix)),
            themeResId = R.style.Theme_HSSudoku2
        )
    }

    private val host = ParticipantEntity.Host("host's uid", "my name is host")
    private val guest = ParticipantEntity.Guest("guest's uid", "my name is guest")
    private val ready = ParticipantEntity.ReadyGuest(guest.uid, guest.displayName)
    private val playing = ParticipantEntity.Playing(
        uid = guest.uid,
        displayName = guest.displayName,
        matrix = CustomMatrix(
            listOf(
                listOf(0, 1, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )
    )

    @Test
    fun 내가_참여자_일_떄_컨트롤러_UI_테스트() {
        fragmentScenario.onFragment {
            it.setStatus(guest)
        }

        onView(withId(R.id.viewSilhouette)).check(matches(isDisplayed()))
        onView(withText("Ready")).check(matches(isDisplayed()))


        fragmentScenario.onFragment {
            it.setStatus(ready)
        }

        onView(withId(R.id.viewSilhouette)).check(matches(isDisplayed()))
        onView(withText("Cancel Ready")).check(matches(isDisplayed()))


        fragmentScenario.onFragment {
            it.setValues(generatedMatrix)
            it.setStatus(playing)
        }

        onView(withId(R.id.viewSilhouette)).check(matches(not(isDisplayed())))
        onView(withText("Cancel Ready")).check(doesNotExist())

//        runBlocking { delay(5000) }
//
//        onView(withText("1"))
//            .perform(
//                ViewActions.repeatedlyUntil(
//                    GeneralSwipeAction(
//                        Swipe.SLOW,
//                        GeneralLocation.TOP_CENTER,
//                        GeneralLocation.BOTTOM_RIGHT,
//                        Press.FINGER
//                    ),
//                    withText("4"),
//                    2
//                )
//            )
    }

    @Test
    fun 내가_방장_일_떄_컨트롤러_UI_테스트() {
        fragmentScenario.onFragment {
            it.setStatus(host)
        }
        onView(withText("1")).check(doesNotExist())
        onView(withId(R.id.viewSilhouette)).check(matches(isDisplayed()))
        onView(withText("Start Game")).check(matches(isDisplayed()))

        fragmentScenario.onFragment {
            it.setStatus(host)
        }
        onView(withText("1")).check(doesNotExist())
        onView(withId(R.id.viewSilhouette)).check(matches(isDisplayed()))
        onView(withText("Start Game")).check(matches(isDisplayed()))

        fragmentScenario.onFragment {
            it.setValues(generatedMatrix)
            it.setStatus(playing)
        }

        onView(withId(R.id.viewSilhouette)).check(matches(not(isDisplayed())))
        onView(withText("Cancel Ready")).check(doesNotExist())

//        runBlocking { delay(5000) }
//
//        onView(withText("1"))
//            .perform(
//                ViewActions.repeatedlyUntil(
//                    GeneralSwipeAction(
//                        Swipe.FAST,
//                        GeneralLocation.TOP_CENTER,
//                        GeneralLocation.BOTTOM_RIGHT,
//                        Press.FINGER
//                    ),
//                    withText("4"),
//                    2
//                )
//            )
    }

    @Test
    fun 게임_컨트롤러_UI_테스트2_for_guest() {
        fragmentScenario.onFragment {
            it.setStatus(ParticipantEntity.Guest("uid", "name"))
        }
        runBlocking { delay(500) }


        val stage = listOf(
            listOf(3, 0, 0, 2),
            listOf(0, 1, 4, 0),
            listOf(1, 0, 0, 4),
            listOf(0, 3, 2, 0)
        )

        fragmentScenario.onFragment {
            val getMatrix = CustomMatrix(stage)
            it.setValues(stage)
            it.setStatus(ParticipantEntity.Playing("uid", "name", matrix = getMatrix))

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
        runBlocking { delay(500) }
    }
}