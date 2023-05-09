package kr.co.hs.sudoku.usecase.battle

import kr.co.hs.sudoku.repository.battle.BattleRepository

class BattleMatrixControlUseCase(
    private val repository: BattleRepository,
    private val uid: String
) {
    lateinit var mutableMatrix: List<MutableList<Int>>

    suspend operator fun invoke(row: Int, column: Int, value: Int?) {
        if (!this::mutableMatrix.isInitialized) {
            val participant = repository.getParticipant(uid)
            val matrix = participant!!.matrix
            mutableMatrix = List(matrix.rowCount) { matrix[it].toMutableList() }
        }

        mutableMatrix[row][column] = value ?: 0
        repository.updateParticipantMatrix(uid, mutableMatrix)
    }
}