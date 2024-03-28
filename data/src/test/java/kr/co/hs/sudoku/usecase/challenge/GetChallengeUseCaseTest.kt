package kr.co.hs.sudoku.usecase.challenge

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.FirebaseTest
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.challenge.impl.ChallengeEntityImpl
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.repository.RepositoryException
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.usecase.SudokuRandomGenerateUseCase
import kr.co.hs.sudoku.usecase.UseCaseFlow
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
class GetChallengeUseCaseTest : FirebaseTest() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: ChallengeRepository

    @Inject
    lateinit var usecase: GetChallengeUseCase

    @Inject
    lateinit var sudokuGenerator: SudokuRandomGenerateUseCase

    lateinit var challengeEntity: ChallengeEntity

    override fun onBefore() = runTest(timeout = Duration.INFINITE) {
        super.onBefore()
        hiltRule.inject()
        repository.createChallenge(
            ChallengeEntityImpl(
                CustomMatrix(
                    sudokuGenerator(
                        SudokuRandomGenerateUseCase.Param(9, 50.0),
                        this
                    ).toValueTable()
                )
            )
        )

        challengeEntity = repository.getChallenges(Date(), 1).first()
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        val error1 = (usecase("").first() as? UseCaseFlow.Result.Exception)?.t
        assertTrue(error1 is RepositoryException.EmptyIdException)

        val error2 = (usecase("asdasdas").first() as? UseCaseFlow.Result.Error)?.e
        assertEquals(GetChallengeUseCase.ChallengeNotFound, error2)

        val entity = (usecase(
            challengeEntity.challengeId
        ).first() as? UseCaseFlow.Result.Success)?.data
        assertEquals(challengeEntity.challengeId, entity?.challengeId)
    }
}