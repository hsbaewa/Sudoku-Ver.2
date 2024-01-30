package kr.co.hs.sudoku.feature.profile

import androidx.recyclerview.widget.DiffUtil

class UserListDiffItemCallback : DiffUtil.ItemCallback<UserListItem>() {
    override fun areItemsTheSame(oldItem: UserListItem, newItem: UserListItem) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: UserListItem, newItem: UserListItem) =
        oldItem == newItem
}