package kr.co.hs.sudoku.model.stage

interface MutableSudokuLine :
    SudokuLine,
    MutableCellList<Int>,
    SudokuStrategyRule