package kr.co.hs.sudoku.feature.profile

import androidx.recyclerview.widget.DiffUtil

class OnlineUserListDiffItemCallback : DiffUtil.ItemCallback<OnlineUserListItem>() {
    override fun areItemsTheSame(oldItem: OnlineUserListItem, newItem: OnlineUserListItem) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: OnlineUserListItem, newItem: OnlineUserListItem) =
        oldItem == newItem
}