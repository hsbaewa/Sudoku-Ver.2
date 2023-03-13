package kr.co.hs.sudoku.model.stage

interface SudokuBox :
    CellTable<Int>,
    SudokuStrategyRule {
    fun getAvailableValueInBox(): List<Int>
}