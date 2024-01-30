package kr.co.hs.sudoku.feature.profile

import kr.co.hs.sudoku.databinding.LayoutListItemUserLabelEmptyBinding

class UserListEmptyItemViewHolder(binding: LayoutListItemUserLabelEmptyBinding) :
    UserListItemViewHolder<UserListItem.EmptyMessage>(binding.root) {
    override fun onBind(item: UserListItem.EmptyMessage) {}
}