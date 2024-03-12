package kr.co.hs.sudoku

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.core.Stage
import kr.co.hs.sudoku.core.impl.IntCoordinateCellEntityImpl
import kr.co.hs.sudoku.core.impl.MutableStageImpl
import kotlin.math.sqrt

class SudokuStageBuilder(val list: List<List<Int>>) : SudokuBuilder {
    override fun build(): Flow<Stage> = flow {
        val stage = with(list) {
            MutableStageImpl(
                sqrt(size.toDouble()).toInt(),
                sqrt(size.toDouble()).toInt(),
                mapIndexed { row, columns ->
                    columns.mapIndexed { column, value ->
                        IntCoordinateCellEntityImpl(row, column).apply {
                            value.takeIf { it > 0 }?.run { toImmutable(this) }
                        }
                    }.toMutableList()
                }
            )
        }
        emit(stage)
    }
}