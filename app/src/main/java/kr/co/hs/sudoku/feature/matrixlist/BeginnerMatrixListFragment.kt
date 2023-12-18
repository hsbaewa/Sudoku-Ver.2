package kr.co.hs.sudoku.feature.matrixlist

import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.BeginnerMatrixRepository
import kr.co.hs.sudoku.repository.stage.MatrixRepository

class BeginnerMatrixListFragment : MatrixListFragment() {
    override val repository: MatrixRepository<IntMatrix> = BeginnerMatrixRepository()
}