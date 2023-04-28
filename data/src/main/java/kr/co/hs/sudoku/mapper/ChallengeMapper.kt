package kr.co.hs.sudoku.mapper

import kr.co.hs.sudoku.model.challenge.ChallengeModel
import kr.co.hs.sudoku.model.challenge.impl.ChallengeEntityImpl
import kr.co.hs.sudoku.model.matrix.CustomMatrix

object ChallengeMapper {
    fun ChallengeModel.toDomain() = ChallengeEntityImpl(
        challengeId = id,
        createdAt = createdAt?.toDate(),
        matrix = CustomMatrix(
            boxSize = boxSize,
            boxCount = boxCount,
            rowCount = rowCount,
            columnCount = columnCount,
            matrix = List(rowCount) { row ->
                List(columnCount) { column ->
                    matrix[row * rowCount + column]
                }
            }
        )
    )
}