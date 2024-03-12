package kr.co.hs.sudoku.core.history.impl

import kr.co.hs.sudoku.core.IntCoordinateCellEntity
import kr.co.hs.sudoku.core.history.HistoryItem
import kr.co.hs.sudoku.core.history.HistoryQueue
import java.util.TreeSet

class HistoryQueueImpl : HistoryQueue {

    private val treeSet = TreeSet<HistoryItem>()

    override fun push(
        cell: IntCoordinateCellEntity,
        time: Long,
        completed: Boolean
    ) = cell.run {
        when {
            isEmpty() -> treeSet.add(HistoryItem.Removed(row, column, time))
            isMutable() -> treeSet.add(HistoryItem.Set(row, column, time, getValue(), completed))
            else -> false
        }
    }

    override fun pop(toTimeStamp: Long) =
        treeSet.filter { it.time <= toTimeStamp }
            .also { treeSet.removeAll(it) }
            .takeIf { it.isNotEmpty() }

    override fun isEmpty() = treeSet.isEmpty()
}