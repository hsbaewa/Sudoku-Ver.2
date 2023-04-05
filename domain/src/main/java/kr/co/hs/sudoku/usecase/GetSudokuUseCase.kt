package kr.co.hs.sudoku.usecase

import kotlinx.coroutines.flow.Flow

interface GetSudokuUseCase {
    operator fun invoke(level: Int): Flow<BuildSudokuUseCase>
}