package kr.co.hs.sudoku.usecase.challenge

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.FirebaseTest
import kr.co.hs.sudoku.core.IntCoordinateCellEntity
import kr.co.hs.sudoku.core.Timer
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.usecase.SudokuBuildUseCase
import kr.co.hs.sudoku.usecase.UseCaseFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class GetLeaderBoardUseCaseTest : FirebaseTest() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var sudokuGenerator: SudokuBuildUseCase

    @Inject
    lateinit var createChallenge: CreateChallengeUseCase

    @Inject
    lateinit var clearChallenge: ClearChallengeUseCase

    @Inject
    lateinit var usecase: GetLeaderBoardUseCase

    lateinit var challenge: ChallengeEntity


    private class TimerTestImpl : Timer {
        override fun start() = getCurrentTime().also { start = it }
        private var start = -1L
        private fun getCurrentTime() = System.currentTimeMillis()
        override fun getStartTime() = start.takeIf { it >= 0 } ?: start()
        override fun getPassedTime() = getCurrentTime() - getStartTime()
        override fun getTickInterval() = 50L
        override fun finish() = (getCurrentTime() - getStartTime()).also { finish = it }
        private var finish = -1L
        override fun getFinishTime() = finish.takeIf { it >= 0 } ?: finish()
    }

    override fun onBefore() = runTest(timeout = 30.seconds) {
        super.onBefore()
        hiltRule.inject()

        val matrix = listOf(
            listOf(3, 4, 1, 2),
            listOf(2, 1, 4, 3),
            listOf(1, 2, 3, 4),
            listOf(4, 3, 2, 0)
        )
        val stage = sudokuGenerator(matrix, this)
        stage.addValueChangedListener(object : IntCoordinateCellEntity.ValueChangedListener {
            override fun onChanged(cell: IntCoordinateCellEntity) {
                println()
            }
        })
        val timer = TimerTestImpl()
        stage.setTimer(timer)
        timer.start()
        runBlocking { delay(2000) }
        val passedTime = timer.getPassedTime()
        println(passedTime)
        stage[3, 3] = 1


        createChallenge(stage.toValueTable())
            .collect {
                challenge = it.getSuccessData() ?: throw Exception("failed create challenge")
            }


        val clearResult = clearChallenge(challenge, stage).firstOrNull()
        clearResult as UseCaseFlow.Result.Success
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        val leaderBoard = usecase(challenge)
            .mapNotNull { (it as? UseCaseFlow.Result.Success)?.data }
            .toList()

        assertEquals(1, leaderBoard.size)
    }
}