package kr.co.hs.sudoku.feature.challenge.dashboard

import kr.co.hs.sudoku.model.challenge.ChallengeEntity

sealed class ChallengeDashboardListItem {
    abstract val id: String

    object TitleItem : ChallengeDashboardListItem() {
        override val id = "TitleItem"
    }

    data class ChallengeItem(val challengeEntity: ChallengeEntity) : ChallengeDashboardListItem() {
        override val id: String
            get() = challengeEntity.challengeId
    }
}