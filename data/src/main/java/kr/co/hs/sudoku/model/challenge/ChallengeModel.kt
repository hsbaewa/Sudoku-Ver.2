package kr.co.hs.sudoku.model.challenge

import com.google.firebase.Timestamp

data class ChallengeModel(
    val boxSize: Int,
    val boxCount: Int,
    val rowCount: Int,
    val columnCount: Int,
    val matrix: List<Int>,
    val createdAt: Timestamp? = null
) {
    var id: String? = null

    constructor() : this(
        -1, -1, -1, -1, emptyList(), null
    )
}