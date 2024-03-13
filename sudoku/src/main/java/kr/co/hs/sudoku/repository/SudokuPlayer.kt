package kr.co.hs.sudoku.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kr.co.hs.sudoku.core.IntCoordinateCellEntity
import kr.co.hs.sudoku.core.Stage

class SudokuPlayer(
    private val stage: Stage,
    private var threashold: Long
) {

    val flow: Flow<IntCoordinateCellEntity> = callbackFlow {
        stage.addValueChangedListener(object : IntCoordinateCellEntity.ValueChangedListener {
            override fun onChanged(cell: IntCoordinateCellEntity) {
                trySend(cell)
            }
        })
        stage.play(0, 0)
        close()
    }

    private suspend fun Stage.play(row: Int, column: Int) {
        if (isSudokuClear()) {
            threashold = 0
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
            delay((threashold / 5) + (available.size * (threashold / 8)))
            if (isSudokuClear()) {
                threashold = 0
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
                delay(threashold)
                this[x, y] = it
            }

            if (y == columnCount - 1) {
                play(x + 1, 0)
            } else {
                play(x, y + 1)
            }
        }
    }
}