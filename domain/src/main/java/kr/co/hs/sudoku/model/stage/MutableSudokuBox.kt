package kr.co.hs.sudoku.model.stage

interface MutableSudokuBox :
    SudokuBox,
    MutableCellTable<Int>,
    SudokuStrategyRule