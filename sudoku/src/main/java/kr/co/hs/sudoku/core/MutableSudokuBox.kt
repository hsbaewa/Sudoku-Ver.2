package kr.co.hs.sudoku.core

interface MutableSudokuBox :
    SudokuBox,
    MutableCellTable<Int>,
    SudokuStrategyRule