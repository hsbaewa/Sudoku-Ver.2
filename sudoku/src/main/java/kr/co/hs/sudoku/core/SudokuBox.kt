package kr.co.hs.sudoku.core

interface SudokuBox :
    CellTable<Int>,
    SudokuStrategyRule {
    fun getAvailableValueInBox(): List<Int>
}