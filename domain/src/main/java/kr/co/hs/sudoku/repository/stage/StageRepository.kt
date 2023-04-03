package kr.co.hs.sudoku.repository.stage

import kr.co.hs.sudoku.model.stage.Stage

sealed interface StageRepository {
    fun getStage(level: Int): Stage
    fun getLastLevel(): Int

    interface BeginnerStageRepository : StageRepository
    interface IntermediateStageRepository : StageRepository
    interface AdvancedStageRepository : StageRepository
}