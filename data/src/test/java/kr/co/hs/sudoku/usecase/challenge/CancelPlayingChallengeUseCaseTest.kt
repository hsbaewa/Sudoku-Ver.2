package kr.co.hs.sudoku.usecase.challenge

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
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
class CancelPlayingChallengeUseCaseTest : FirebaseTest() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var usecase: CancelPlayingChallengeUseCase

    @Inject
    lateinit var createChallenge: CreateChallengeUseCase

    @Inject
    lateinit var startChallenge: StartChallengeUseCase

    lateinit var challenge: ChallengeEntity

    @Inject
    lateinit var sudokuGenerator: SudokuBuildUseCase

    @Inject
    lateinit var clearChallenge: ClearChallengeUseCase

    @Inject
    lateinit var getChallenge: GetChallengeUseCase

    override fun onBefore() = runTest(timeout = Duration.INFINITE) {
        super.onBefore()
        hiltRule.inject()

        createChallenge(4, 50.0)
            .mapNotNull { it.getSuccessData() }
            .filter { startChallenge(it).firstOrNull() as? UseCaseFlow.Result.Success != null }
            .collect { challenge = it }

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
        stage[3, 3] = 1


        clearChallenge(challenge, stage).firstOrNull() as UseCaseFlow.Result.Success

        challenge = (getChallenge(
            challenge.challengeId
        ).firstOrNull() as? UseCaseFlow.Result.Success)?.data
            ?: throw Exception("error")
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        challenge = (getChallenge(
            challenge.challengeId
        ).firstOrNull() as? UseCaseFlow.Result.Success)?.data
            ?: throw Exception("error")

        usecase(challenge).firstOrNull() as UseCaseFlow.Result.Success

        challenge = (getChallenge(
            challenge.challengeId
        ).firstOrNull() as? UseCaseFlow.Result.Success)?.data
            ?: throw Exception("error")
        startChallenge(challenge).firstOrNull() as UseCaseFlow.Result.Success
    }
}