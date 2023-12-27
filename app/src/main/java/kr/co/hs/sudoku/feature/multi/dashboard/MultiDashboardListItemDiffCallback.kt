package kr.co.hs.sudoku.feature.multi.dashboard

import androidx.recyclerview.widget.DiffUtil

class MultiDashboardListItemDiffCallback : DiffUtil.ItemCallback<MultiDashboardListItem>() {
    override fun areItemsTheSame(oldItem: MultiDashboardListItem, newItem: MultiDashboardListItem) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: MultiDashboardListItem, newItem: MultiDashboardListItem) =
        oldItem == newItem
}