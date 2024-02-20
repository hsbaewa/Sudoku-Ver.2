package kr.co.hs.sudoku.feature.leaderboard

import androidx.recyclerview.widget.DiffUtil

class LeaderBoardListItemDiffCallback : DiffUtil.ItemCallback<LeaderBoardListItem>() {
    override fun areItemsTheSame(
        oldItem: LeaderBoardListItem,
        newItem: LeaderBoardListItem
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: LeaderBoardListItem,
        newItem: LeaderBoardListItem
    ): Boolean {
        return oldItem == newItem
    }
}