package kr.co.hs.sudoku.model.stage.history

interface HistoryWriter {
    fun push(row: Int, column: Int, value: Int?, completed: Boolean)
    fun toHistoryList(): List<HistoryItem>
    fun clearAllHistory()
}