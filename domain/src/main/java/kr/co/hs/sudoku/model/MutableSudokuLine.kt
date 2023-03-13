package kr.co.hs.sudoku.model

interface MutableSudokuLine :
    SudokuLine,
    MutableCellList<Int>,
    SudokuStrategyRule