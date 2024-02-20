package kr.co.hs.sudoku.model.logs

import java.util.Date

interface ChallengeClearLogEntity {
    val challengeId: String
    val grade: Long
    val clearAt: Date
    val uid: String
    val record: Long
}