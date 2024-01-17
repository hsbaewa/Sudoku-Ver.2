package kr.co.hs.sudoku.feature.challenge.dashboard

import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.rank.RankerEntity
import java.util.Date

sealed class ChallengeDashboardListItem {
    abstract val id: String
    abstract val order: Int

    data class TitleItem(var date: Date?) : ChallengeDashboardListItem() {
        override val id = "TitleItem_${date?.time}"
        override val order = 0
    }

    object MatrixHeaderItem : ChallengeDashboardListItem() {
        override val id = "MatrixHeaderItem"
        override val order = 1
    }

    data class MatrixItem(val matrix: IntMatrix) : ChallengeDashboardListItem() {
        override val id = "MatrixItem"
        override val order = 2
    }

    object RankHeaderItem : ChallengeDashboardListItem() {
        override val id = "RankHeaderItem"
        override val order = 3
    }

    data class RankItem(val rankEntity: RankerEntity) : ChallengeDashboardListItem() {
        override val id: String
            get() = rankEntity.uid
        override val order = 4
    }

    data class MyRankItem(val rankEntity: RankerEntity) : ChallengeDashboardListItem() {
        override val id: String
            get() = rankEntity.uid
        override val order = 5
    }

    data class ChallengeStartItem(val challengeEntity: ChallengeEntity) :
        ChallengeDashboardListItem() {
        override val id: String
            get() = challengeEntity.challengeId
        override val order = 6
    }
}