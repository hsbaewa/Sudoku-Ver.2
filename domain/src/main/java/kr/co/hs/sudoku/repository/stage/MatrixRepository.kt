package kr.co.hs.sudoku.repository.stage

import kr.co.hs.sudoku.model.matrix.IntMatrix

interface MatrixRepository<out T : IntMatrix> {
    suspend fun getList(): List<T>
}