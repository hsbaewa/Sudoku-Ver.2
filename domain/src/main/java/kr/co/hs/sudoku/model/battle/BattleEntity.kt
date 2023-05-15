package kr.co.hs.sudoku.model.battle

import java.util.Date

sealed interface BattleEntity {
    val id: String
    val host: String
    val startingMatrix: List<List<Int>>
    val createdAt: Date
    val participantSize: Int
    val participants: Array<BattleParticipantEntity?>

    fun addParticipant(participant: BattleParticipantEntity) {
        synchronized(this) {
            participants
                .takeIf { list ->
                    val result = list.find { it?.uid == participant.uid }
                    result == null
                }
                ?.run {
                    // null 인 아이템의 index를 찾아서 추가함.
                    indexOfFirst { it == null }
                        .takeIf { it >= 0 }
                        ?.run { participants[this] = participant }
                }

        }
    }

    data class WaitingBattleEntity(
        override val id: String,
        override val host: String,
        override val startingMatrix: List<List<Int>>,
        override val createdAt: Date,
        private val participantMaxSize: Int,
        override val participantSize: Int
    ) : BattleEntity {
        override val participants: Array<BattleParticipantEntity?> =
            Array(participantMaxSize) { null }
    }

    data class PendingBattleEntity(
        override val id: String,
        override val host: String,
        override val startingMatrix: List<List<Int>>,
        override val createdAt: Date,
        val pendingAt: Date,
        private val participantMaxSize: Int,
        override val participantSize: Int
    ) : BattleEntity {
        override val participants: Array<BattleParticipantEntity?> =
            Array(participantMaxSize) { null }
    }

    data class RunningBattleEntity(
        override val id: String,
        override val host: String,
        override val startingMatrix: List<List<Int>>,
        override val createdAt: Date,
        val startedAt: Date,
        private val participantMaxSize: Int,
        override val participantSize: Int
    ) : BattleEntity {
        override val participants: Array<BattleParticipantEntity?> =
            Array(participantMaxSize) { null }
    }

    data class ClearedBattleEntity(
        override val id: String,
        override val host: String,
        override val startingMatrix: List<List<Int>>,
        override val createdAt: Date,
        var winner: String,
        private val participantMaxSize: Int,
        override val participantSize: Int
    ) : BattleEntity {
        override val participants: Array<BattleParticipantEntity?> =
            Array(participantMaxSize) { null }
    }
}