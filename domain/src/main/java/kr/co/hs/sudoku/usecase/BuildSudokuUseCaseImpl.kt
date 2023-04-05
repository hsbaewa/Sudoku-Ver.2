package kr.co.hs.sudoku.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.model.stage.impl.IntCoordinateCellEntityImpl
import kr.co.hs.sudoku.model.stage.impl.MutableStageImpl

class BuildSudokuUseCaseImpl(override val matrix: IntMatrix) : BuildSudokuUseCase {
    private val cellMatrix: List<MutableList<IntCoordinateCellEntity>> =
        List(matrix.rowCount) { row ->
            MutableList(matrix.columnCount) { column ->
                IntCoordinateCellEntityImpl(row, column).apply {
                    matrix[row][column]
                        .takeIf { it > 0 }
                        ?.run { toImmutable(this) }
                }
            }
        }

    override fun invoke(): Flow<Stage> {
        return flow {
            val stage = MutableStageImpl(matrix.boxSize, matrix.boxCount, cellMatrix)
            emit(stage)
        }
    }

    override fun isImmutable(row: Int, column: Int) = !isMutable(row, column)
    override fun isMutable(row: Int, column: Int) = matrix[row][column] == 0
}