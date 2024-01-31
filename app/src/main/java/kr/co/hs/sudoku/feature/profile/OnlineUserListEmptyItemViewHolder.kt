package kr.co.hs.sudoku.feature.profile

import kr.co.hs.sudoku.databinding.LayoutListItemUserLabelEmptyBinding

class OnlineUserListEmptyItemViewHolder(binding: LayoutListItemUserLabelEmptyBinding) :
    OnlineUserListItemViewHolder<OnlineUserListItem.EmptyMessage>(binding.root) {
    override fun onBind(item: OnlineUserListItem.EmptyMessage) {}
}