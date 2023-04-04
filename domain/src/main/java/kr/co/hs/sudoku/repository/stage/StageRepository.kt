package kr.co.hs.sudoku.repository.stage

import kr.co.hs.sudoku.model.stage.StageBuilder

sealed interface StageRepository : List<StageBuilder> {
    suspend fun doRequestStageList(): Boolean

    interface BeginnerStageRepository : StageRepository
    interface IntermediateStageRepository : StageRepository
    interface AdvancedStageRepository : StageRepository
}