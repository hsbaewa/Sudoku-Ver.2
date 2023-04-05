package kr.co.hs.sudoku.usecase

import kotlinx.coroutines.flow.Flow
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity

interface PlaySudokuUseCase {
    operator fun invoke(): Flow<IntCoordinateCellEntity>
}