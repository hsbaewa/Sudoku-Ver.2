package kr.co.hs.sudoku.model.logs

import com.google.firebase.Timestamp

sealed interface LogModel {
    var id: String
    val createdAt: Timestamp
    val uid: String

    interface ChallengeClear : LogModel {
        val challengeId: String
        val record: Long
    }

    interface BattleClear : LogModel {
        val battleWith: List<String>
        val record: Long
        val battleId: String
    }
}