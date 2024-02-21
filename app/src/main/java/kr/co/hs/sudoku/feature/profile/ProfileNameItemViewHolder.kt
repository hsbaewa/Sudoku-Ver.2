package kr.co.hs.sudoku.feature.profile

import kr.co.hs.sudoku.databinding.LayoutListItemProfileDisplayNameBinding

class ProfileNameItemViewHolder(private val binding: LayoutListItemProfileDisplayNameBinding) :
    ProfileItemViewHolder(binding.root) {
    override fun onBind(item: ProfileItem?) {
        when (item) {
            is ProfileItem.DisplayName -> onBindDisplayName(item)
            else -> {}
        }
    }

    private fun onBindDisplayName(item: ProfileItem.DisplayName) = with(binding) {
        tvDisplayName.text = buildString {
            item.nationFlag?.let { flag ->
                append(flag)
                append(" ")
            }
            append(item.name)
        }
    }

    override fun onViewRecycled() {}
}