package kr.co.hs.sudoku.feature.play

interface PlayPresenter {
    fun onStartSudoku()
    fun onCompleteSudoku()
    fun onChangedSudokuCell(row: Int, column: Int, value: Int)
}