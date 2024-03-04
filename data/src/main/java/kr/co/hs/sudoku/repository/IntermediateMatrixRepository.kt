package kr.co.hs.sudoku.repository

import kr.co.hs.sudoku.datasource.StageRemoteSource
import kr.co.hs.sudoku.mapper.StageMapper.toDomain
import kr.co.hs.sudoku.model.matrix.IntermediateMatrix
import kr.co.hs.sudoku.repository.stage.MatrixRepository
import javax.inject.Inject

class IntermediateMatrixRepository
@Inject constructor(
    private val stageRemoteSource: StageRemoteSource
) : MatrixRepository<IntermediateMatrix> {

    override suspend fun getList(): List<IntermediateMatrix> =
        stageRemoteSource.getIntermediateGenerateMask().map { it.matrix.toDomain() }
}