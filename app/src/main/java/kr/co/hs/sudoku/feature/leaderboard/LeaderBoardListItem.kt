package kr.co.hs.sudoku.feature.leaderboard

import kr.co.hs.sudoku.model.battle.BattleLeaderBoardEntity
import kr.co.hs.sudoku.model.rank.RankerEntity

sealed interface LeaderBoardListItem {
    val id: String
    val isMine: Boolean
    val grade: Long
    val displayName: String

    data class BattleItem(
        val entity: BattleLeaderBoardEntity,
        override val isMine: Boolean = false
    ) : LeaderBoardListItem {
        override val id: String
            get() = entity.uid
        override val grade: Long
            get() = entity.ranking
        override val displayName: String
            get() = entity.displayName ?: ""
    }

    data class BattleItemForMine(
        val entity: BattleLeaderBoardEntity
    ) : LeaderBoardListItem {
        override val id: String
            get() = entity.uid
        override val isMine: Boolean
            get() = true
        override val grade: Long
            get() = entity.ranking
        override val displayName: String
            get() = entity.displayName ?: ""

    }

    data class ChallengeItem(
        val entity: RankerEntity,
        override val isMine: Boolean
    ) : LeaderBoardListItem {
        override val id: String
            get() = entity.uid
        override val grade: Long
            get() = entity.rank
        override val displayName: String
            get() = entity.displayName
    }

    data class ChallengeItemForMine(
        val entity: RankerEntity
    ) : LeaderBoardListItem {
        override val id: String
            get() = entity.uid
        override val isMine: Boolean
            get() = true
        override val grade: Long
            get() = entity.rank
        override val displayName: String
            get() = entity.displayName
    }

    data class Empty(override val grade: Long) : LeaderBoardListItem {
        override val id: String
            get() = "Empty_$grade"
        override val isMine: Boolean
            get() = false
        override val displayName: String
            get() = ""
    }
}