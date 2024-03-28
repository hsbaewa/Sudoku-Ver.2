package kr.co.hs.sudoku.usecase.challenge

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.FirebaseTest
import kr.co.hs.sudoku.core.IntCoordinateCellEntity
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


@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class StartChallengeUseCaseTest : FirebaseTest() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var usecase: StartChallengeUseCase

    @Inject
    lateinit var createChallenge: CreateChallengeUseCase

    lateinit var challenge: ChallengeEntity

    @Inject
    lateinit var getChallenge: GetChallengeUseCase


    @Inject
    lateinit var sudokuGenerator: SudokuBuildUseCase

    @Inject
    lateinit var clearChallenge: ClearChallengeUseCase

    override fun onBefore() = runTest(timeout = Duration.INFINITE) {
        super.onBefore()
        hiltRule.inject()
        val result =
            createChallenge(9, 50.0).firstOrNull()

        challenge = result?.getSuccessData() ?: throw Exception("error")
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        challenge = (getChallenge(
            challenge.challengeId
        ).firstOrNull() as? UseCaseFlow.Result.Success)?.data
            ?: throw Exception("error")

        assertFalse(challenge.isPlaying)
        assertFalse(challenge.isComplete)

        val result = usecase(challenge).firstOrNull() as? UseCaseFlow.Result.Success
        assertNotNull(result)

        challenge = (getChallenge(
            challenge.challengeId
        ).firstOrNull() as? UseCaseFlow.Result.Success)?.data
            ?: throw Exception("error")
        assertTrue(challenge.isPlaying)
        assertFalse(challenge.isComplete)


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
        val timer = TestTimer()
        stage.setTimer(timer)
        timer.start()
        runBlocking { delay(2000) }
        val passedTime = timer.getPassedTime()
        println(passedTime)
        stage[3, 3] = 1


        clearChallenge(challenge, stage).firstOrNull() as UseCaseFlow.Result.Success
        challenge = (getChallenge(
            challenge.challengeId
        ).firstOrNull() as? UseCaseFlow.Result.Success)?.data
            ?: throw Exception("error")
        assertFalse(challenge.isPlaying)
        assertTrue(challenge.isComplete)

        val startError = usecase(challenge).firstOrNull() as? UseCaseFlow.Result.Error
        assertEquals(StartChallengeUseCase.AlreadyClear, startError?.e)
    }
}