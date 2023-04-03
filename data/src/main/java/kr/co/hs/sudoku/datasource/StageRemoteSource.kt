package kr.co.hs.sudoku.datasource

import kr.co.hs.sudoku.model.sudoku.StageModel

interface StageRemoteSource {
    suspend fun getBeginnerGenerateMask(): List<StageModel.AutoGenStageModel>
    suspend fun getIntermediateGenerateMask(): List<StageModel.AutoGenStageModel>
    suspend fun getAdvancedGenerateMask(): List<StageModel.AutoGenStageModel>
}