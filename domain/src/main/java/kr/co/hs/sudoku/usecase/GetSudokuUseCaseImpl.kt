package kr.co.hs.sudoku.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.stage.MatrixRepository

class GetSudokuUseCaseImpl<T : IntMatrix>(
    private val repository: MatrixRepository<T>
) : GetSudokuUseCase {
    override fun invoke(level: Int): Flow<BuildSudokuUseCase> {
        return flow {
            val matrix = repository.getList()[level]
            emit(
                AutoGenerateSudokuUseCase(
                    boxSize = matrix.boxSize,
                    boxCount = matrix.boxCount,
                    filterMask = matrix
                )
            )
        }
    }
}