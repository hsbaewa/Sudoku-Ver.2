package kr.co.hs.sudoku.feature.battle

import androidx.recyclerview.widget.DiffUtil
import kr.co.hs.sudoku.model.battle.ParticipantEntity

class ParticipantItemDiffCallback : DiffUtil.ItemCallback<ParticipantEntity>() {
    override fun areItemsTheSame(
        oldItem: ParticipantEntity,
        newItem: ParticipantEntity
    ): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(
        oldItem: ParticipantEntity,
        newItem: ParticipantEntity
    ): Boolean {
        return oldItem == newItem
    }
}