package kr.co.hs.sudoku.model.battle2

import kr.co.hs.sudoku.model.matrix.IntMatrix
import java.util.Date

sealed interface BattleEntity : Cloneable {
    val id: String
    val host: String
    val createdAt: Date
    val startingMatrix: IntMatrix
    val maxParticipants: Int
    val participantSize: Int
    val participants: Set<ParticipantEntity>
    fun init(participants: Set<ParticipantEntity>)

    object Invalid : BattleEntity {
        override val id: String
            get() = ""
        override val host: String
            get() = throw Exception("invalid battle entity")
        override val createdAt: Date
            get() = throw Exception("invalid battle entity")
        override val startingMatrix: IntMatrix
            get() = throw Exception("invalid battle entity")
        override val maxParticipants: Int
            get() = 0
        override val participantSize: Int
            get() = 0
        override val participants: Set<ParticipantEntity>
            get() = emptySet()

        override fun init(participants: Set<ParticipantEntity>) {}
    }

    data class Opened(
        override val id: String,
        override val host: String,
        override val createdAt: Date,
        override val startingMatrix: IntMatrix,
        override val maxParticipants: Int,
        override val participantSize: Int
    ) : BattleEntity {
        override val participants by this::_participants
        private val _participants = HashSet<ParticipantEntity>()

        override fun init(participants: Set<ParticipantEntity>) {
            this._participants.clear()
            this._participants.addAll(participants)
        }

        override fun clone() = Opened(
            id, host, createdAt, startingMatrix, maxParticipants, participantSize
        ).also {
            it.init(participants)
        }
    }

    data class Pending(
        override val id: String,
        override val host: String,
        override val createdAt: Date,
        override val startingMatrix: IntMatrix,
        override val maxParticipants: Int,
        override val participantSize: Int,
        val pendedAt: Date
    ) : BattleEntity {
        override val participants by this::_participants
        private val _participants = HashSet<ParticipantEntity>()

        override fun init(participants: Set<ParticipantEntity>) {
            this._participants.clear()
            this._participants.addAll(participants)
        }

        override fun clone() = Pending(
            id, host, createdAt, startingMatrix, maxParticipants, participantSize, pendedAt
        ).also {
            it.init(participants)
        }
    }

    data class Playing(
        override val id: String,
        override val host: String,
        override val createdAt: Date,
        override val startingMatrix: IntMatrix,
        override val maxParticipants: Int,
        override val participantSize: Int,
        val playedAt: Date
    ) : BattleEntity {
        override val participants by this::_participants
        private val _participants = HashSet<ParticipantEntity>()

        override fun init(participants: Set<ParticipantEntity>) {
            this._participants.clear()
            this._participants.addAll(participants)
        }

        override fun clone() = Playing(
            id, host, createdAt, startingMatrix, maxParticipants, participantSize, playedAt
        ).also {
            it.init(participants)
        }
    }

    data class Closed(
        override val id: String,
        override val host: String,
        override val createdAt: Date,
        override val startingMatrix: IntMatrix,
        override val maxParticipants: Int,
        override val participantSize: Int,
        val winner: String
    ) : BattleEntity, Cloneable {
        override val participants by this::_participants
        private val _participants = HashSet<ParticipantEntity>()

        override fun init(participants: Set<ParticipantEntity>) {
            this._participants.clear()
            this._participants.addAll(participants)
        }

        override fun clone() = Closed(
            id, host, createdAt, startingMatrix, maxParticipants, participantSize, winner
        ).also {
            it.init(participants)
        }
    }

    public override fun clone(): BattleEntity = when (this) {
        is Closed -> clone()
        Invalid -> Invalid
        is Opened -> clone()
        is Pending -> clone()
        is Playing -> clone()
    }
}