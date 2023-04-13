package kr.co.hs.sudoku.model.gamelog.impl

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kr.co.hs.sudoku.model.gamelog.CellLogEntity
import kr.co.hs.sudoku.model.gamelog.CellLogStreamEntity
import java.util.*

class CellLogStreamEntityImpl(
    private val logQueue: Queue<CellLogEntity>
) : CellLogStreamEntity {

    private val stream = MutableSharedFlow<CellLogEntity>(replay = logQueue.size)

    override suspend fun pop(passedTime: Long) =
        with(logQueue) {
            filter { it.time <= passedTime }
                .also { removeAll(it.toSet()) }
                .asFlow()
        }.run { stream.emitAll(this) }

    override fun getLogStream() = stream
}