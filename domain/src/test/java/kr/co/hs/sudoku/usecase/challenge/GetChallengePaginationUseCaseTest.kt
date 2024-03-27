package kr.co.hs.sudoku.usecase.challenge

import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.data.TestChallengeDataSource
import kr.co.hs.sudoku.data.TestRecordDataSource
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.repository.challenge.TestChallengeRepository
import kr.co.hs.sudoku.usecase.UseCaseFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date
import kotlin.time.Duration

class GetChallengePaginationUseCaseTest {
    private lateinit var repository: ChallengeRepository
    private lateinit var usecase: GetChallengePaginationUseCase

    @Before
    fun before() = runTest {
        repository = TestChallengeRepository(TestChallengeDataSource(), TestRecordDataSource())
        usecase = GetChallengePaginationUseCase(repository)
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        val resultList = mutableListOf<ChallengeEntity>()
        usecase(Date(0), 10)
            .mapNotNull { (it as? UseCaseFlow.Result.Success)?.data }
            .toList(resultList)

        assertTrue(resultList.isEmpty())

        usecase(Date(1), 10)
            .mapNotNull { (it as? UseCaseFlow.Result.Success)?.data }
            .toList(resultList)

        assertEquals(1, resultList.size)
        resultList.clear()

        usecase(Date(2), 10)
            .mapNotNull { (it as? UseCaseFlow.Result.Success)?.data }
            .toList(resultList)

        assertEquals(2, resultList.size)
        resultList.clear()

        usecase(Date(), 10)
            .mapNotNull { (it as? UseCaseFlow.Result.Success)?.data }
            .toList(resultList)

        assertEquals(3, resultList.size)
        resultList.clear()

        usecase(Date(), 1)
            .mapNotNull { (it as? UseCaseFlow.Result.Success)?.data }
            .toList(resultList)

        assertEquals(1, resultList.size)
        assertEquals(2L, resultList.first().createdAt?.time)
    }
}