package kr.co.hs.sudoku.model.challenge

import java.util.Date

interface ChallengeEntity {
    val challengeId: String
    val matrix: List<List<Int>>
    val createdAt: Date?
    var isPlaying: Boolean
    var startPlayAt: Date?
    var isComplete: Boolean
    var relatedUid: String?
}