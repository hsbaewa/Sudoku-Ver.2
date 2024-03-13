package kr.co.hs.sudoku.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.core.Stage
import kr.co.hs.sudoku.core.impl.IntCoordinateCellEntityImpl
import kr.co.hs.sudoku.core.impl.MutableStageImpl
import kotlin.math.sqrt

internal class SudokuStageGenerator(
    private val fixCell: List<List<Int>>
) : SudokuBuilder {

    override val flow: Flow<Stage> = flow {
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

    override suspend fun build(scope: CoroutineScope): Stage = scope
        .async { withContext(Dispatchers.Default) { flow.first() } }
        .await()

    override fun build(): Stage = runBlocking { withContext(Dispatchers.Default) { flow.first() } }
}