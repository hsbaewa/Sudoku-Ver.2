package kr.co.hs.sudoku.core.history

import kr.co.hs.sudoku.core.IntCoordinateCellEntity

interface HistoryQueue {
    fun push(cell: IntCoordinateCellEntity, time: Long, completed: Boolean): Boolean
    fun pop(toTimeStamp: Long): List<HistoryItem>?
    fun isEmpty(): Boolean
}