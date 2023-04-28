package kr.co.hs.sudoku.model.stage.history

sealed interface HistoryItem {
    val row: Int
    val column: Int
    val time: Long

    data class Set(
        override val row: Int,
        override val column: Int,
        override val time: Long,
        val value: Int,
        val isCompleted: Boolean = false
    ) : HistoryItem, Comparable<HistoryItem> {
        override fun compareTo(other: HistoryItem) = time.compareTo(other.time)
    }

    data class Removed(
        override val row: Int,
        override val column: Int,
        override val time: Long
    ) : HistoryItem, Comparable<HistoryItem> {
        override fun compareTo(other: HistoryItem) = time.compareTo(other.time)
    }
}