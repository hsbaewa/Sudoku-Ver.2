package kr.co.hs.sudoku.model

interface MutableSudokuBox :
    SudokuBox,
    MutableCellTable<Int>,
    SudokuStrategyRule