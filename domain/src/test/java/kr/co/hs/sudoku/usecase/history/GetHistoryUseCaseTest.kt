package kr.co.hs.sudoku.usecase.history

import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.data.TestHistoryDataSource
import kr.co.hs.sudoku.repository.history.TestHistoryRepository
import kr.co.hs.sudoku.usecase.UseCaseFlow
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration

class GetHistoryUseCaseTest {
    private lateinit var historyRepository: TestHistoryRepository
    private lateinit var useCase: GetHistoryUseCase

    @Before
    fun before() {
        historyRepository = TestHistoryRepository(TestHistoryDataSource())
        useCase = GetHistoryUseCase(historyRepository)
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        useCase("asda").collect {
            when (it) {
                is UseCaseFlow.Result.Error -> {}
                is UseCaseFlow.Result.Exception -> throw it.t
                is UseCaseFlow.Result.Success -> throw Exception("is not success")
            }
        }

        useCase("0").collect {
            when (it) {
                is UseCaseFlow.Result.Error -> throw Exception("error")
                is UseCaseFlow.Result.Exception -> throw it.t
                is UseCaseFlow.Result.Success -> {}
            }
        }
    }
}