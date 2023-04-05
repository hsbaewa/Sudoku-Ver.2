package kr.co.hs.sudoku.usecase

import kotlinx.coroutines.flow.Flow
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.Stage

interface BuildSudokuUseCase {
    val matrix: IntMatrix
    fun isImmutable(row: Int, column: Int): Boolean
    fun isMutable(row: Int, column: Int): Boolean

    operator fun invoke(): Flow<Stage>

}