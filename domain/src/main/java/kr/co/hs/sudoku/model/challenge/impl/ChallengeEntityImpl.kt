package kr.co.hs.sudoku.model.challenge.impl

import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.matrix.IntMatrix
import java.util.Date

data class ChallengeEntityImpl(
    override val challengeId: String,
    override val matrix: IntMatrix,
    override val createdAt: Date? = null
) : ChallengeEntity {
    @Suppress("unused")
    constructor(
        matrix: IntMatrix
    ) : this("", matrix, null)

    override var isPlaying = false
    override var startPlayAt: Date? = null
    override var isComplete = false
    override var relatedUid: String? = null
}