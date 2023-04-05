package kr.co.hs.sudoku.repository

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kr.co.hs.sudoku.datasource.StageRemoteSource
import kr.co.hs.sudoku.datasource.impl.StageRemoteSourceFromConfig
import kr.co.hs.sudoku.mapper.StageMapper.toDomain
import kr.co.hs.sudoku.model.matrix.AdvancedMatrix
import kr.co.hs.sudoku.repository.stage.MatrixRepository

class AdvancedMatrixRepository : MatrixRepository<AdvancedMatrix> {
    private var stageRemoteSource: StageRemoteSource =
        StageRemoteSourceFromConfig(FirebaseRemoteConfig.getInstance())

    internal fun setRemoteSource(stageRemoteSource: StageRemoteSource) {
        this.stageRemoteSource = stageRemoteSource
    }

    override suspend fun getList(): List<AdvancedMatrix> =
        stageRemoteSource.getAdvancedGenerateMask().map { it.matrix.toDomain() }
}