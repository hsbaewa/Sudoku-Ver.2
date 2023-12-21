package kr.co.hs.sudoku.feature.multi

import androidx.recyclerview.widget.DiffUtil

class MultiPlayListItemDiffCallback : DiffUtil.ItemCallback<MultiPlayListItem>() {
    override fun areItemsTheSame(oldItem: MultiPlayListItem, newItem: MultiPlayListItem) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: MultiPlayListItem, newItem: MultiPlayListItem) =
        oldItem == newItem
}