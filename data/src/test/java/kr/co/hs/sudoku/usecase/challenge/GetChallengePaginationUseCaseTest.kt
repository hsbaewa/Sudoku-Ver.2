package kr.co.hs.sudoku.usecase.challenge

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.FirebaseTest
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.usecase.SudokuRandomGenerateUseCase
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
class GetChallengePaginationUseCaseTest : FirebaseTest() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var challengeCreateUseCase: CreateChallengeUseCase

    @Inject
    lateinit var usecase: GetChallengePaginationUseCase

    @Inject
    lateinit var sudokuGenerator: SudokuRandomGenerateUseCase

    lateinit var entity1: ChallengeEntity
    lateinit var entity2: ChallengeEntity

    private suspend fun createDummyChallenge() =
        challengeCreateUseCase(9, 50.0).firstOrNull()?.getSuccessData()


    override fun onBefore() = runTest(timeout = Duration.INFINITE) {
        super.onBefore()
        hiltRule.inject()

        entity1 = createDummyChallenge() ?: throw Exception("failed create challenge")
        entity2 = createDummyChallenge() ?: throw Exception("failed create challenge")
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        val list = mutableListOf<ChallengeEntity>()
        usecase(5)
            .mapNotNull { (it as? UseCaseFlow.Result.Success)?.data }
            .toList(list)

        var pick = list.find { it.matrix == entity1.matrix }
        assertNotNull(pick)

        pick = list.find { it.matrix == entity2.matrix }
        assertNotNull(pick)
    }
}