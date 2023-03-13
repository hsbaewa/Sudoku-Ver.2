package kr.co.hs.sudoku.model

interface SudokuLine :
    CellList<Int>,
    SudokuStrategyRule {
    fun getAvailableValueInLine(): List<Int>
}