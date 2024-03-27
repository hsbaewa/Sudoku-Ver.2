package kr.co.hs.sudoku.model.history

import java.util.Date

sealed class HistoryEntity {

    abstract val id: String
    abstract val createdAt: Date
    abstract val uid: String

    data class ChallengeClear(
        override val id: String,
        override val createdAt: Date,
        override val uid: String,
        val challengeId: String,
        val grade: Long,
        val clearAt: Date,
        val record: Long
    ) : HistoryEntity()

    data class BattleClear(
        override val id: String,
        override val createdAt: Date,
        override val uid: String,
        val battleWith: List<String>,
        val record: Long,
        val battleId: String
    ) : HistoryEntity()
}