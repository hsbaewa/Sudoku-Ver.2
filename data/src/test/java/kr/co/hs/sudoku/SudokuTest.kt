package kr.co.hs.sudoku

import kr.co.hs.sudoku.repository.StageRepositoryImpl
import kr.co.hs.sudoku.usecase.GetStageUseCaseImpl
import kr.co.hs.sudoku.usecase.UseCaseResult
import org.junit.Test

class SudokuTest {

    @Test
    fun getStageTest() {
        val getStageUseCase = GetStageUseCaseImpl(StageRepositoryImpl())
        val stage = when (val result = getStageUseCase()) {
            is UseCaseResult.Error -> TODO()
            is UseCaseResult.Success -> result.data
        }
        println(stage)
    }
}