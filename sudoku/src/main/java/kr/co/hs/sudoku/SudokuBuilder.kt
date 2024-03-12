package kr.co.hs.sudoku

import kotlinx.coroutines.flow.Flow
import kr.co.hs.sudoku.core.Stage

interface SudokuBuilder {
    fun build(): Flow<Stage>
}