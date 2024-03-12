package kr.co.hs.sudoku.core

interface MutableSudokuLine :
    SudokuLine,
    MutableCellList<Int>,
    SudokuStrategyRule