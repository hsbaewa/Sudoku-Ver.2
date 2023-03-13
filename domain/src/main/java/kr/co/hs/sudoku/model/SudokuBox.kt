package kr.co.hs.sudoku.model

interface SudokuBox :
    CellTable<Int>,
    SudokuStrategyRule {
    fun getAvailableValueInBox(): List<Int>
}