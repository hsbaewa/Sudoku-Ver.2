package kr.co.hs.sudoku.repository

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kr.co.hs.sudoku.datasource.StageRemoteSource
import kr.co.hs.sudoku.datasource.impl.StageRemoteSourceFromConfig
import kr.co.hs.sudoku.mapper.StageMapper.toDomain
import kr.co.hs.sudoku.model.stage.StageBuilder
import kr.co.hs.sudoku.repository.stage.StageRepository

class BeginnerStageRepositoryImpl(
    private val list: ArrayList<StageBuilder> = ArrayList()
) : StageRepository.BeginnerStageRepository, List<StageBuilder> by list {

    private var stageRemoteSource: StageRemoteSource =
        StageRemoteSourceFromConfig(FirebaseRemoteConfig.getInstance())

    internal fun setRemoteSource(stageRemoteSource: StageRemoteSource) {
        this.stageRemoteSource = stageRemoteSource
    }

    override suspend fun doRequestStageList() = with(list) {
        clear()
        addAll(stageRemoteSource.getBeginnerGenerateMask().map { it.toDomain() })
    }
}