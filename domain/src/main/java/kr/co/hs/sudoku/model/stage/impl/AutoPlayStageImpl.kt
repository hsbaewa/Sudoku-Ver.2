package kr.co.hs.sudoku.model.stage.impl

import kotlinx.coroutines.delay
import kr.co.hs.sudoku.model.stage.AutoPlayStage
import kr.co.hs.sudoku.model.stage.Stage

class AutoPlayStageImpl(
    private val stage: Stage,
    private var threashold: Long
) : AutoPlayStage {

    override suspend fun play() {
        stage.play(0, 0)
    }

    private suspend fun Stage.play(row: Int, column: Int) {
        if (isCompleted()) {
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
            if (isCompleted()) {
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