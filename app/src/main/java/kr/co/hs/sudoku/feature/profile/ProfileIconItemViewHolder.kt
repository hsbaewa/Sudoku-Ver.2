package kr.co.hs.sudoku.feature.profile

import coil.dispose
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutListItemProfileIconBinding
import kr.co.hs.sudoku.extension.CoilExt.loadProfileImage

class ProfileIconItemViewHolder(private val binding: LayoutListItemProfileIconBinding) :
    ProfileItemViewHolder(binding.root) {
    override fun onBind(item: ProfileItem?) {
        when (item) {
            is ProfileItem.Icon -> with(binding) {
                ivIcon.loadProfileImage(item.url, R.drawable.ic_person)
            }

            else -> {}
        }
    }

    override fun onViewRecycled() {
        binding.ivIcon.dispose()
    }
}