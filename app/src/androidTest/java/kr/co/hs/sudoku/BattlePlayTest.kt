package kr.co.hs.sudoku

import android.view.View
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kr.co.hs.sudoku.feature.multiplay.MultiPlayActivity
import kr.co.hs.sudoku.model.battle.ParticipantEntity
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import org.hamcrest.CoreMatchers.any
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeoutException


@Suppress("TestFunctionName", "NonAsciiCharacters")
@RunWith(AndroidJUnit4::class)
class BattlePlayTest {


    @Test
    fun 화면_시작() {
        val matrix = testStartingMatrix

        val scenario = launch(MultiPlayActivity::class.java)
        scenario.onActivity {
            it.startingMatrix = CustomMatrix(matrix)
        }

        scenario.onActivity {
            val entity = ParticipantEntity.Host("host", "host")
            it.controlBoard {
                it.setStatus(entity)
            }
            it.setUserProfile(entity)
        }
        runBlocking { delay(500) }

        scenario.onActivity {
            val entity = ParticipantEntity.Guest("guest", "guest")
            it.viewerBoard {
                it.setStatus(entity)
            }
            it.setOpponentProfile(entity)
        }
        runBlocking { delay(500) }

        scenario.onActivity {
            val entity = ParticipantEntity.ReadyGuest("guest", "guest")
            it.viewerBoard {
                it.setStatus(entity)
            }
            it.setOpponentProfile(entity)
        }
        runBlocking { delay(500) }



        scenario.onActivity {

        }

        scenario.onActivity {
            it.startingMatrix = CustomMatrix(testStage1)
        }
        runBlocking { delay(500) }

        scenario.onActivity {
            val userEntity = ParticipantEntity.Playing("h", "h", matrix = CustomMatrix(testStage1))
            val opponentEntity =
                ParticipantEntity.Playing("g", "g", matrix = CustomMatrix(testStage2))

            it.controlBoard {
                it.setStatus(userEntity)
            }
            it.viewerBoard {
                it.setStatus(opponentEntity)
            }

            it.startTimer(null)
        }
        runBlocking { delay(500) }

        scenario.onActivity {
            val userEntity = ParticipantEntity.Playing(
                "h", "h",
                matrix = CustomMatrix(
                    listOf(
                        listOf(3, 4, 0, 2),
                        listOf(0, 1, 4, 0),
                        listOf(1, 0, 0, 4),
                        listOf(0, 3, 2, 0)
                    )
                )
            )
            val opponentEntity = ParticipantEntity.Playing(
                "g", "g",
                matrix = CustomMatrix(
                    listOf(
                        listOf(2, 2, 0, 3),
                        listOf(0, 4, 1, 0),
                        listOf(1, 0, 0, 4),
                        listOf(0, 3, 2, 0)
                    )
                )
            )
            it.controlBoard {
                it.setStatus(userEntity)
            }
            it.viewerBoard {
                it.setStatus(opponentEntity)
            }
        }
        runBlocking { delay(500) }

        scenario.onActivity {
            val userEntity = ParticipantEntity.Playing(
                "h", "h",
                matrix = CustomMatrix(
                    listOf(
                        listOf(3, 4, 1, 2),
                        listOf(0, 1, 4, 0),
                        listOf(1, 0, 0, 4),
                        listOf(0, 3, 2, 0)
                    )
                )
            )
            it.controlBoard { it.setStatus(userEntity) }
        }
        runBlocking { delay(500) }


        scenario.onActivity {
            val userEntity = ParticipantEntity.Playing(
                "h", "h",
                matrix = CustomMatrix(
                    listOf(
                        listOf(3, 4, 1, 2),
                        listOf(2, 1, 4, 3),
                        listOf(1, 2, 3, 4),
                        listOf(0, 3, 2, 1)
                    )
                )
            )
            it.controlBoard {
                it.setStatus(userEntity)
            }
        }

        runBlocking { delay(5000) }

        var clearTime = 0L
        scenario.onActivity {
            val userEntity = ParticipantEntity.Playing(
                "h", "h",
                matrix = CustomMatrix(
                    listOf(
                        listOf(3, 4, 1, 2),
                        listOf(2, 1, 4, 3),
                        listOf(1, 2, 3, 4),
                        listOf(4, 3, 2, 1)
                    )
                )
            )
            it.controlBoard {
                it.setStatus(userEntity)
            }

            it.stopTimer()
            it.controlBoard {
                clearTime = it.getClearTime()
            }
        }

        val millis = clearTime % 1000
        val allSeconds = clearTime / 1000
        val seconds = allSeconds % 60
        val allMinutes = allSeconds / 60
        val minutes = allMinutes % 60
        val hour = allMinutes / 60
        val strClear = String.format("%d:%02d:%02d.%03d", hour, minutes, seconds, millis)
        onView(withText(strClear)).check(matches(isDisplayed()))
    }


    val testStartingMatrix = listOf(
        listOf(1, 0, 0, 1),
        listOf(0, 1, 1, 0),
        listOf(1, 0, 0, 1),
        listOf(0, 1, 1, 0)
    )

    val testStage1 = listOf(
        listOf(3, 0, 0, 2),
        listOf(0, 1, 4, 0),
        listOf(1, 0, 0, 4),
        listOf(0, 3, 2, 0)
    )

    val testStage2 = listOf(
        listOf(2, 0, 0, 3),
        listOf(0, 4, 1, 0),
        listOf(1, 0, 0, 4),
        listOf(0, 3, 2, 0)
    )

    class WaitUntilVisibleAction(private val timeout: Long) : ViewAction {

        override fun getConstraints(): Matcher<View> {
            return any(View::class.java)
        }

        override fun getDescription(): String {
            return "wait up to $timeout milliseconds for the view to become visible"
        }

        override fun perform(uiController: UiController, view: View) {

            val endTime = System.currentTimeMillis() + timeout

            do {
                if (view.visibility == View.VISIBLE) return
                uiController.loopMainThreadForAtLeast(50)
            } while (System.currentTimeMillis() < endTime)

            throw PerformException.Builder()
                .withActionDescription(description)
                .withCause(TimeoutException("Waited $timeout milliseconds"))
                .withViewDescription(HumanReadables.describe(view))
                .build()
        }
    }

}