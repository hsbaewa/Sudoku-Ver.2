package kr.co.hs.sudoku.feature.challenge2

import androidx.recyclerview.widget.DiffUtil

class ChallengeDashboardListItemDiffCallback : DiffUtil.ItemCallback<ChallengeDashboardListItem>() {
    override fun areItemsTheSame(
        oldItem: ChallengeDashboardListItem,
        newItem: ChallengeDashboardListItem
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: ChallengeDashboardListItem,
        newItem: ChallengeDashboardListItem
    ) = oldItem == newItem
}