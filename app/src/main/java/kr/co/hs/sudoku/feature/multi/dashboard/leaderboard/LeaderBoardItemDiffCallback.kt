package kr.co.hs.sudoku.feature.multi.dashboard.leaderboard

import androidx.recyclerview.widget.DiffUtil

class LeaderBoardItemDiffCallback : DiffUtil.ItemCallback<LeaderBoardItem>() {
    override fun areItemsTheSame(oldItem: LeaderBoardItem, newItem: LeaderBoardItem) =
        oldItem.entity.uid == newItem.entity.uid

    override fun areContentsTheSame(oldItem: LeaderBoardItem, newItem: LeaderBoardItem) =
        oldItem.entity == newItem.entity
}