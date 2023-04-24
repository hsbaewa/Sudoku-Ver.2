package kr.co.hs.sudoku.model.stage.history.impl

import kr.co.hs.sudoku.model.stage.history.HistoryItem
import kr.co.hs.sudoku.model.stage.history.HistoryWriter
import kr.co.hs.sudoku.repository.timer.Timer
import java.util.TreeSet

class HistoryWriterImpl(private val timer: Timer) : HistoryWriter {

    private val treeSet = TreeSet<HistoryItem>()

    override fun push(row: Int, column: Int, value: Int?, completed: Boolean) {
        value.takeIf { it != null && it > 0 }
            ?.run {
                treeSet.add(HistoryItem.Set(row, column, timer.getPassedTime(), this, completed))
            }
            ?: treeSet.add(HistoryItem.Removed(row, column, timer.getPassedTime()))
    }

    override fun toHistoryList() = treeSet.toList()
    override fun clearAllHistory() = treeSet.clear()
}