package kr.co.hs.sudoku.usecase

import kotlinx.coroutines.flow.Flow
import kr.co.hs.sudoku.model.stage.Stage

interface GetStageUseCase<T : Stage> {
    operator fun invoke(level: Int): Flow<T>
}