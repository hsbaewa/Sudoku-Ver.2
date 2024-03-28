package kr.co.hs.sudoku.usecase.challenge

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.FirebaseTest
import kr.co.hs.sudoku.core.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.usecase.SudokuBuildUseCase
import kr.co.hs.sudoku.usecase.UseCaseFlow
import kr.co.hs.sudoku.usecase.history.GetHistoryListUseCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date
import javax.inject.Inject
import kotlin.time.Duration

@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class ClearChallengeUseCaseTest : FirebaseTest() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var usecase: ClearChallengeUseCase

    @Inject
    lateinit var createChallenge: CreateChallengeUseCase

    lateinit var challenge: ChallengeEntity

    @Inject
    lateinit var sudokuGenerator: SudokuBuildUseCase

    @Inject
    lateinit var getHistoryUseCase: GetHistoryListUseCase

    override fun onBefore() = runTest(timeout = Duration.INFINITE) {
        super.onBefore()
        hiltRule.inject()

        createChallenge(9, 50.0)
            .onEach { challenge = it.getSuccessData() ?: throw Exception("error") }
            .launchIn(this)
    }


    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        val matrix = listOf(
            listOf(3, 4, 1, 2),
            listOf(2, 1, 4, 3),
            listOf(1, 2, 3, 4),
            listOf(4, 3, 2, 0)
        )
        val stage = sudokuGenerator(matrix, this)
        stage.addValueChangedListener(object : IntCoordinateCellEntity.ValueChangedListener {
            override fun onChanged(cell: IntCoordinateCellEntity) {}
        })
        val timer = TestTimer()
        stage.setTimer(timer)
        timer.start()
        runBlocking { delay(2000) }

        var result = usecase(challenge, stage).firstOrNull() as? UseCaseFlow.Result.Success
        TestCase.assertNull(result)

        stage[3, 3] = 1

        result = usecase(challenge, stage).firstOrNull() as? UseCaseFlow.Result.Success
        TestCase.assertNotNull(result)


        val list = getHistoryUseCase(Date(), 1)
            .mapNotNull { (it as? UseCaseFlow.Result.Success)?.data }
            .toList()

        assertTrue(list.isNotEmpty())
    }
}