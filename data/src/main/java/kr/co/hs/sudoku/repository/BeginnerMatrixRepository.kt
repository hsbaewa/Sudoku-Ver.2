package kr.co.hs.sudoku.repository

import kr.co.hs.sudoku.datasource.StageRemoteSource
import kr.co.hs.sudoku.mapper.StageMapper.toDomain
import kr.co.hs.sudoku.model.matrix.BeginnerMatrix
import kr.co.hs.sudoku.repository.stage.MatrixRepository
import javax.inject.Inject

class BeginnerMatrixRepository
@Inject constructor(
    private val stageRemoteSource: StageRemoteSource
) : MatrixRepository<BeginnerMatrix> {

    override suspend fun getList(): List<BeginnerMatrix> =
        stageRemoteSource.getBeginnerGenerateMask().map { it.matrix.toDomain() }
}