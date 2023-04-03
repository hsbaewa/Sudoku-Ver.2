package kr.co.hs.sudoku.repository.stage

import kr.co.hs.sudoku.model.stage.Stage

sealed interface StageRepository : List<Stage> {
    suspend fun doRequestStageList(): Boolean

    interface BeginnerStageRepository : StageRepository
    interface IntermediateStageRepository : StageRepository
    interface AdvancedStageRepository : StageRepository
}