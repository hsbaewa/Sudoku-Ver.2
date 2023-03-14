package kr.co.hs.sudoku.model.stage.impl

import kotlinx.coroutines.delay
import kr.co.hs.sudoku.model.stage.AutoPlayStage
import kr.co.hs.sudoku.model.stage.Stage

class AutoPlayStageImpl(
    private val stage: Stage,
    private val threashold: Long
) : AutoPlayStage {

    override suspend fun play() {
        stage.play(0, 0)
    }

    private suspend fun Stage.play(row: Int, column: Int) {
        if (row == rowCount)
            return

        val cell = getCell(row, column)

        val available = getAvailable(row, column).shuffled()
        if (available.isEmpty()) {
            val flattenList = toList()
            val idx = flattenList.indexOf(cell)
            flattenList.subList(idx, size()).forEach {
                if (it.isMutable()) {
                    delay(threashold / 5)
                    it.toEmpty()
                }
            }
            return
        }
        available.forEach {
            delay((threashold / 5) + (available.size * (threashold / 8)))
            if (isCompleted())
                return

            if (!cell.isImmutable()) {
                delay(threashold)
                this[row, column] = it
            }

            if (column == columnCount - 1) {
                play(row + 1, 0)
            } else {
                play(row, column + 1)
            }
        }
    }
}