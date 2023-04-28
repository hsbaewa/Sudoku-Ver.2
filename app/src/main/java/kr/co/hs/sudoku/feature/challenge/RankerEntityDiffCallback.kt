package kr.co.hs.sudoku.feature.challenge

import androidx.recyclerview.widget.DiffUtil
import kr.co.hs.sudoku.model.rank.RankerEntity

class RankerEntityDiffCallback : DiffUtil.ItemCallback<RankerEntity>() {
    override fun areItemsTheSame(oldItem: RankerEntity, newItem: RankerEntity): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: RankerEntity, newItem: RankerEntity): Boolean {
        return oldItem == newItem
    }
}