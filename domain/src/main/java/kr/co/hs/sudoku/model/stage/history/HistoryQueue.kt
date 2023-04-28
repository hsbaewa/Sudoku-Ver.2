package kr.co.hs.sudoku.model.stage.history

import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity

interface HistoryQueue {
    fun push(cell: IntCoordinateCellEntity, time: Long, completed: Boolean): Boolean
    fun pop(toTimeStamp: Long): List<HistoryItem>?
    fun isEmpty(): Boolean
}