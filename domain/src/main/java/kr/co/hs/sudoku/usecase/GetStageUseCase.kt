package kr.co.hs.sudoku.usecase

import kr.co.hs.sudoku.model.stage.Stage

interface GetStageUseCase<T : Stage> {
    operator fun invoke(): UseCaseResult<T, Throwable>
}