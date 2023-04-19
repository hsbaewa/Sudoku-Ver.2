package kr.co.hs.sudoku.model.challenge

import kr.co.hs.sudoku.model.matrix.IntMatrix
import java.util.Date

interface ChallengeEntity {
    val challengeId: String?
    val matrix: IntMatrix
    val createdAt: Date?
}