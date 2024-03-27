package kr.co.hs.sudoku.usecase.challenge

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.data.TestChallengeDataSource
import kr.co.hs.sudoku.data.TestRecordDataSource
import kr.co.hs.sudoku.repository.RepositoryException
import kr.co.hs.sudoku.repository.challenge.TestChallengeRepository
import kr.co.hs.sudoku.usecase.UseCaseFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration

class GetChallengeUseCaseTest {
    private lateinit var usecase: GetChallengeUseCase

    @Before
    fun before() {
        val repository = TestChallengeRepository(
            TestChallengeDataSource(),
            TestRecordDataSource()
        )
        usecase = GetChallengeUseCase(repository)
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        val error = (usecase("").first() as? UseCaseFlow.Result.Exception)?.t
        assertTrue(error is RepositoryException.EmptyIdException)

        val data = (usecase("0").first() as? UseCaseFlow.Result.Success)?.data
        assertEquals("0", data?.challengeId)
    }
}