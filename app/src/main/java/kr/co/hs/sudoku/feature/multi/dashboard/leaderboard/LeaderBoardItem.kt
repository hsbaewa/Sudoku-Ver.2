package kr.co.hs.sudoku.feature.multi.dashboard.leaderboard

import kr.co.hs.sudoku.model.battle.BattleLeaderBoardEntity

sealed interface LeaderBoardItem {
    val entity: BattleLeaderBoardEntity

    data class ListItem(
        override val entity: BattleLeaderBoardEntity,
        val isMine: Boolean
    ) : LeaderBoardItem

    data class MyItem(
        override val entity: BattleLeaderBoardEntity
    ) : LeaderBoardItem
}