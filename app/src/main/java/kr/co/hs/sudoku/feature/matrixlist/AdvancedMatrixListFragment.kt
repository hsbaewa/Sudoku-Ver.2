package kr.co.hs.sudoku.feature.matrixlist

import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.AdvancedMatrixRepository
import kr.co.hs.sudoku.repository.stage.MatrixRepository

class AdvancedMatrixListFragment : MatrixListFragment() {
    override val repository: MatrixRepository<IntMatrix> = AdvancedMatrixRepository()
}