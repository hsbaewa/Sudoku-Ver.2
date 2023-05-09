package kr.co.hs.sudoku.model.battle

import java.util.Date

sealed interface BattleEntity {
    val id: String
    val host: String
    val startingMatrix: List<List<Int>>
    val createdAt: Date

    data class WaitingBattleEntity(
        override val id: String,
        override val host: String,
        override val startingMatrix: List<List<Int>>,
        override val createdAt: Date,
    ) : BattleEntity

    data class RunningBattleEntity(
        override val id: String,
        override val host: String,
        override val startingMatrix: List<List<Int>>,
        override val createdAt: Date,
        val startedAt: Date,
    ) : BattleEntity

    data class ClearedBattleEntity(
        override val id: String,
        override val host: String,
        override val startingMatrix: List<List<Int>>,
        override val createdAt: Date,
        var winner: String
    ) : BattleEntity
}