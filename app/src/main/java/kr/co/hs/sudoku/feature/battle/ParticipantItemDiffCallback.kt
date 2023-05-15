package kr.co.hs.sudoku.feature.battle

import androidx.recyclerview.widget.DiffUtil
import kr.co.hs.sudoku.model.battle.BattleParticipantEntity

class ParticipantItemDiffCallback : DiffUtil.ItemCallback<BattleParticipantEntity>() {
    override fun areItemsTheSame(
        oldItem: BattleParticipantEntity,
        newItem: BattleParticipantEntity
    ): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(
        oldItem: BattleParticipantEntity,
        newItem: BattleParticipantEntity
    ): Boolean {
        return oldItem == newItem
    }
}