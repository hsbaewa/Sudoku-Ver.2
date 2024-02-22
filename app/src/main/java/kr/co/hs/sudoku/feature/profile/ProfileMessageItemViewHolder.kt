package kr.co.hs.sudoku.feature.profile

import kr.co.hs.sudoku.databinding.LayoutListItemProfileMessageBinding

class ProfileMessageItemViewHolder(private val binding: LayoutListItemProfileMessageBinding) :
    ProfileItemViewHolder(binding.root) {
    override fun onBind(item: ProfileItem?) {
        when (item) {
            is ProfileItem.Message -> onBindMessage(item)
            else -> {}
        }
    }

    private fun onBindMessage(item: ProfileItem.Message) = with(binding) {
        tvMessage.text = item.message
    }

    override fun onViewRecycled() {}
}