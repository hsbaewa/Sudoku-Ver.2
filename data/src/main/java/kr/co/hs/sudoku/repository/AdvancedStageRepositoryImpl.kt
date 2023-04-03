package kr.co.hs.sudoku.repository

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kr.co.hs.sudoku.datasource.StageRemoteSource
import kr.co.hs.sudoku.datasource.impl.StageRemoteSourceFromConfig
import kr.co.hs.sudoku.mapper.StageMapper.toDomain
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.repository.stage.StageRepository

class AdvancedStageRepositoryImpl(
    private val list: ArrayList<Stage> = ArrayList()
) : StageRepository.AdvancedStageRepository, List<Stage> by list {

    private var stageRemoteSource: StageRemoteSource =
        StageRemoteSourceFromConfig(FirebaseRemoteConfig.getInstance())

    internal fun setRemoteSource(stageRemoteSource: StageRemoteSource) {
        this.stageRemoteSource = stageRemoteSource
    }

    override suspend fun doRequestStageList() =
        list.addAll(stageRemoteSource.getAdvancedGenerateMask().map { it.toDomain() })
}