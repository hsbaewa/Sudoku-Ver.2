package kr.co.hs.sudoku.feature.profile

import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutListItemProfileDividerBinding

class ProfileDividerItemViewHolder(private val binding: LayoutListItemProfileDividerBinding) :
    ProfileItemViewHolder(binding.root) {
    override fun onBind(item: ProfileItem?) {
        when (item) {
            is ProfileItem.Divider -> onBindDivider(item)
            else -> {}
        }
    }

    private fun onBindDivider(item: ProfileItem.Divider) = with(binding.tvHeader) {
        text = when (item.type) {
            ProfileItem.DividerType.Challenge -> getString(R.string.profile_label_challenge_log)
        }
    }

    override fun onViewRecycled() {}
}