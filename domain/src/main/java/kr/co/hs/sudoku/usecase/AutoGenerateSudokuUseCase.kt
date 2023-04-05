package kr.co.hs.sudoku.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.model.stage.impl.IntCoordinateCellEntityImpl
import kr.co.hs.sudoku.model.stage.impl.MutableStageImpl

class AutoGenerateSudokuUseCase(
    override val matrix: IntMatrix,
    private val filterMask: List<List<Int>> = matrix,
) : BuildSudokuUseCase {

    constructor(
        boxSize: Int,
        boxCount: Int,
        filterMask: List<List<Int>>
    ) : this(
        matrix = CustomMatrix(boxSize, boxCount),
        filterMask = filterMask
    )

    private val cellMatrix: List<MutableList<IntCoordinateCellEntity>> =
        List(matrix.rowCount) { row ->
            MutableList(matrix.columnCount) { column ->
                IntCoordinateCellEntityImpl(row, column).apply {
                    matrix.getOrNull(row)?.getOrNull(column)
                        ?.takeIf { it > 0 }
                        ?.run { toImmutable(this) }
                }
            }
        }

    override fun invoke(): Flow<Stage> {
        return flow {
            val stage = MutableStageImpl(matrix.boxSize, matrix.boxCount, cellMatrix)
            stage.generate(0, 0)
            (0 until stage.rowCount).forEach { row ->
                row.takeIf { it < filterMask.size }?.run {


                    (0 until stage.columnCount).forEach { column ->
                        column.takeIf { it < filterMask[row].size }?.run {


                            val cell = stage.getCell(row, column)
                            if (filterMask[row][column] > 0) {
                                if (!cell.isImmutable())
                                    cell.toImmutable()
                            } else {
                                if (cell.isMutable())
                                    cell.toEmpty()
                            }


                        }
                    }


                }
            }
            emit(stage)
        }
    }

    private fun Stage.generate(row: Int, column: Int) {
        if (isCompleted()) {
            return
        }

        var x = row
        var y = column
        var cell = this.getCell(x, y)
        while (cell.isImmutable()) {
            if (y == columnCount - 1) {
                x += 1
                y = 0
            } else {
                y += 1
            }
            cell = this.getCell(x, y)
        }

        val available = getAvailable(x, y).shuffled()
        if (available.isEmpty()) {
            return
        }
        available.forEach {
            if (isCompleted()) {
                return
            }

            with(toList()) {
                val idx = indexOf(getCell(x, y))
                subList(idx, size()).forEach { cell ->
                    if (cell.isMutable()) {
                        cell.toEmpty()
                    }
                }
            }

            if (!cell.isImmutable()) {
                this[x, y] = it
            }

            if (y == columnCount - 1) {
                generate(x + 1, 0)
            } else {
                generate(x, y + 1)
            }
        }
    }

    override fun isImmutable(row: Int, column: Int) = !isMutable(row, column)

    override fun isMutable(row: Int, column: Int) = filterMask[row][column] == 0

}