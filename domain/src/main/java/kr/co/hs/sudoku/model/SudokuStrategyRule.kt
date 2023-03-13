package kr.co.hs.sudoku.model

interface SudokuStrategyRule {
    fun setCellCollection(collection: CellCollection<Int>)
    fun getDuplicatedCells(): CellList<Int>
    fun getDuplicatedCellCount(): Int
    fun getEmptyCells(): CellList<Int>
    fun getEmptyCellCount(): Int
    fun isCompleted(): Boolean
}