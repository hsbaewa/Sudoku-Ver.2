package kr.co.hs.sudoku.feature.profile

import kr.co.hs.sudoku.databinding.LayoutListItemUserHeaderBinding

class UserListHeaderItemViewHolder(private val binding: LayoutListItemUserHeaderBinding) :
    UserListItemViewHolder<UserListItem.Header>(binding.root) {
    override fun onBind(item: UserListItem.Header) {
        binding.tvHeader.text = item.header
    }
}