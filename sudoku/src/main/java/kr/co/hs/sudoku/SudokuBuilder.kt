package kr.co.hs.sudoku

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kr.co.hs.sudoku.core.Stage

interface SudokuBuilder {
    val flow: Flow<Stage>
    fun build(): Stage
    suspend fun build(scope: CoroutineScope): Stage
}