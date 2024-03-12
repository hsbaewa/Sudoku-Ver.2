package kr.co.hs.sudoku

import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.core.Stage
import kr.co.hs.sudoku.core.impl.IntCoordinateCellEntityImpl
import kr.co.hs.sudoku.core.impl.MutableStageImpl
import kotlin.math.sqrt

class SudokuStageGenerator(
    private val fixCell: List<List<Int>>
) : SudokuBuilder {

    override fun build() = flow {
        val stage = with(fixCell) {
            MutableStageImpl(
                sqrt(size.toDouble()).toInt(),
                sqrt(size.toDouble()).toInt(),
                List(size) { row ->
                    MutableList(get(row).size) { column ->
                        IntCoordinateCellEntityImpl(row, column)
                    }
                }
            )
        }
        stage.generate(0, 0)
        fixCell.mapIndexed { row, columns ->
            columns.mapIndexed { column, value ->
                val cell = stage.getCell(row, column)
                if (value > 0) {
                    if (!cell.isImmutable())
                        cell.toImmutable()
                } else {
                    if (cell.isMutable())
                        cell.toEmpty()
                }
            }
        }
        emit(stage)
    }


    private fun Stage.generate(row: Int, column: Int) {
        if (isSudokuClear()) {
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
            if (isSudokuClear()) {
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
}