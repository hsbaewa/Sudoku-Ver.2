package kr.co.hs.sudoku.model.stage

interface SudokuLine :
    CellList<Int>,
    SudokuStrategyRule {
    fun getAvailableValueInLine(): List<Int>
}