package kr.co.hs.sudoku.model.gamelog

sealed interface CellLogEntity {
    val time: Long

    interface Changed : CellLogEntity {
        val row: Int
        val column: Int
        val value: Int
    }
}