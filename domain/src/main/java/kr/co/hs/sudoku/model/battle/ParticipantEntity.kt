package kr.co.hs.sudoku.model.battle

import kr.co.hs.sudoku.model.matrix.EmptyMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.ProfileEntity

sealed interface ParticipantEntity : ProfileEntity {

    data class Host(
        override val uid: String,
        override var displayName: String,
        override var message: String? = null,
        override var iconUrl: String? = null,
        override val locale: LocaleEntity? = null,
        val matrix: IntMatrix = EmptyMatrix()
    ) : ParticipantEntity

    data class Guest(
        override val uid: String,
        override var displayName: String,
        override var message: String? = null,
        override var iconUrl: String? = null,
        override val locale: LocaleEntity? = null
    ) : ParticipantEntity

    data class ReadyGuest(
        override val uid: String,
        override var displayName: String,
        override var message: String? = null,
        override var iconUrl: String? = null,
        override val locale: LocaleEntity? = null,
        val matrix: IntMatrix = EmptyMatrix()
    ) : ParticipantEntity

    data class Playing(
        override val uid: String,
        override var displayName: String,
        override var message: String? = null,
        override var iconUrl: String? = null,
        override val locale: LocaleEntity? = null,
        val matrix: IntMatrix
    ) : ParticipantEntity {
        fun toCleared(record: Long) =
            Cleared(uid, displayName, message, iconUrl, locale, matrix, record)
    }

    data class Cleared(
        override val uid: String,
        override var displayName: String,
        override var message: String? = null,
        override var iconUrl: String? = null,
        override val locale: LocaleEntity? = null,
        val matrix: IntMatrix,
        val record: Long
    ) : ParticipantEntity
}