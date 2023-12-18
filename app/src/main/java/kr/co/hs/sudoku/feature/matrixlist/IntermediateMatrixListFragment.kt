package kr.co.hs.sudoku.feature.matrixlist

import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.IntermediateMatrixRepository
import kr.co.hs.sudoku.repository.stage.MatrixRepository

class IntermediateMatrixListFragment : MatrixListFragment() {
    override val repository: MatrixRepository<IntMatrix> = IntermediateMatrixRepository()
}