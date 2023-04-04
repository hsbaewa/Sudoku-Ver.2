package kr.co.hs.sudoku.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.repository.stage.StageRepository

class GetStageUseCaseImpl(
    private val repository: StageRepository
) : GetStageUseCase<Stage> {

    override fun invoke(level: Int): Flow<Stage> {
        return flow {
            emit(repository[level])
        }
    }
}