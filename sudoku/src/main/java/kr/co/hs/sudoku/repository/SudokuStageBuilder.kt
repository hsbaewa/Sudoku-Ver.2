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

internal class SudokuStageBuilder(val list: List<List<Int>>) : SudokuBuilder {
    override val flow: Flow<Stage> = flow {
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

    override suspend fun build(scope: CoroutineScope): Stage = scope
        .async { withContext(Dispatchers.Default) { flow.first() } }
        .await()

    override fun build(): Stage = runBlocking { withContext(Dispatchers.Default) { flow.first() } }
}