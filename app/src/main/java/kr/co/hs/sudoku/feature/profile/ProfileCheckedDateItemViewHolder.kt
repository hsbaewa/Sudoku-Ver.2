package kr.co.hs.sudoku.feature.profile

import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutListItemProfileLastCheckedBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileCheckedDateItemViewHolder(private val binding: LayoutListItemProfileLastCheckedBinding) :
    ProfileItemViewHolder(binding.root) {
    override fun onBind(item: ProfileItem?) {
        when (item) {
            is ProfileItem.LastChecked -> onBindLastChecked(item)
            else -> {}
        }
    }

    private fun onBindLastChecked(item: ProfileItem.LastChecked) = with(binding) {
        tvLastChecked.text = SimpleDateFormat(
            getString(R.string.profile_last_checked_format),
            Locale.getDefault()
        ).format(item.date)
    }

    override fun onViewRecycled() {}
}