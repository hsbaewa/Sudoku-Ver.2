package kr.co.hs.sudoku.feature.profile

import androidx.recyclerview.widget.DiffUtil

class ProfileItemDiffCallback : DiffUtil.ItemCallback<ProfileItem>() {
    override fun areItemsTheSame(oldItem: ProfileItem, newItem: ProfileItem) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ProfileItem, newItem: ProfileItem) =
        oldItem == newItem
}