package kr.co.hs.sudoku.feature.profile

import kr.co.hs.sudoku.databinding.LayoutListItemUserHeaderBinding

class OnlineUserListHeaderItemViewHolder(private val binding: LayoutListItemUserHeaderBinding) :
    OnlineUserListItemViewHolder<OnlineUserListItem.Header>(binding.root) {
    override fun onBind(item: OnlineUserListItem.Header) {
        binding.tvHeader.text = item.header
    }
}