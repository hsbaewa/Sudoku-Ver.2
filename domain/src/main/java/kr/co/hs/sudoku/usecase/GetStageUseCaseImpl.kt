package kr.co.hs.sudoku.usecase

import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.model.stage.impl.StageBuilderImpl
import kr.co.hs.sudoku.repository.stage.StageRepository

class GetStageUseCaseImpl(
    private val repository: StageRepository
) : GetStageUseCase<Stage> {

    override fun invoke(): UseCaseResult<Stage, Throwable> {
        return try {
            val stageBuilder = StageBuilderImpl()
            stageBuilder.autoGenerate(repository.getAutoGenerateMaskList())
            UseCaseResult.Success(stageBuilder.build())
        } catch (e: Exception) {
            UseCaseResult.Error(e)
        }
    }
}