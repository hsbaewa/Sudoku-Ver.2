package kr.co.hs.sudoku.repository

import kr.co.hs.sudoku.datasource.StageRemoteSource
import kr.co.hs.sudoku.mapper.StageMapper.toDomain
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.repository.stage.StageRepository

class AdvancedStageRepositoryImpl : StageRepository.AdvancedStageRepository {
    private lateinit var stageList: List<Stage>

    suspend fun initialize(stageRemoteSource: StageRemoteSource) {
        stageList = stageRemoteSource.getAdvancedGenerateMask()
            .map { it.toDomain() }
    }

    override fun getStage(level: Int) = takeIf { this::stageList.isInitialized }
        ?.run { stageList[level] } ?: throw NotImplementedError("please call initialize first")

    override fun getLastLevel() = takeIf { this::stageList.isInitialized }
        ?.run { stageList.size } ?: 0
}