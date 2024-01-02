package kr.co.hs.sudoku.feature.multi.dashboard

import kr.co.hs.sudoku.model.battle.BattleEntity

sealed class MultiDashboardListItem {
    abstract val id: String

    data class MultiPlayItem(
        val battleEntity: BattleEntity,
        val isParticipating: Boolean
    ) : MultiDashboardListItem() {
        override val id: String
            get() = battleEntity.id
    }

    data class TitleItem(val title: String) : MultiDashboardListItem() {
        override val id: String
            get() = "[title]$title"
    }

    object CreateNewItem : MultiDashboardListItem() {
        override val id: String = "create new"
    }

    object HeaderUsersItem : MultiDashboardListItem() {
        override val id: String
            get() = "HeaderUsersItem"
    }

    object HeaderOthersItem : MultiDashboardListItem() {
        override val id: String
            get() = "HeaderOthersItem"
    }
}