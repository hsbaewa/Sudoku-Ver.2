package kr.co.hs.sudoku.core

interface SudokuLine :
    CellList<Int>,
    SudokuStrategyRule {
    fun getAvailableValueInLine(): List<Int>
}