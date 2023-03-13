package kr.co.hs.sudoku.usecase

import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.model.stage.impl.MutableStageImpl
import kr.co.hs.sudoku.repository.stage.StageRepository

class GetStageUseCaseImpl(
    private val repository: StageRepository
) : GetStageUseCase<Stage> {

    override fun invoke(): UseCaseResult<Stage, Throwable> {
        val stage = MutableStageImpl(repository.getBoxSize(), repository.getBoxCount())
        stage.generate()
        repository.getStageMask().setMask(stage)
        return UseCaseResult.Success(stage)
    }
}