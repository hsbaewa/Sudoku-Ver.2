package kr.co.hs.sudoku.feature.multilist

import kr.co.hs.sudoku.model.battle.BattleEntity

sealed class MultiPlayListItem {
    abstract val id: String

    data class MultiPlayItem(val battleEntity: BattleEntity) : MultiPlayListItem() {
        override val id: String
            get() = battleEntity.id
    }

    data class TitleItem(val title: String) : MultiPlayListItem() {
        override val id: String
            get() = "[title]$title"
    }

    object CreateNewItem : MultiPlayListItem() {
        override val id: String = "create new"
    }
}