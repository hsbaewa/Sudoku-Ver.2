package kr.co.hs.sudoku.repository

import kr.co.hs.sudoku.datasource.StageRemoteSource
import kr.co.hs.sudoku.mapper.StageMapper.toDomain
import kr.co.hs.sudoku.model.matrix.AdvancedMatrix
import kr.co.hs.sudoku.repository.stage.MatrixRepository
import javax.inject.Inject

class AdvancedMatrixRepository
@Inject constructor(
    private val stageRemoteSource: StageRemoteSource
) : MatrixRepository<AdvancedMatrix> {
    override suspend fun getList(): List<AdvancedMatrix> =
        stageRemoteSource.getAdvancedGenerateMask().map { it.matrix.toDomain() }
}