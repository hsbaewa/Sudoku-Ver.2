package kr.co.hs.sudoku.model.logs

import java.util.Date

interface ChallengeClearLogEntity {
    val id: String
    val challengeId: String
    val grade: Long
    val clearAt: Date
    val uid: String
    val record: Long
    val createdAt: Date
}