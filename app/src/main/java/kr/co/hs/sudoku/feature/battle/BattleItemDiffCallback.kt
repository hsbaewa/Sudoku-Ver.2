package kr.co.hs.sudoku.feature.battle

import androidx.recyclerview.widget.DiffUtil
import kr.co.hs.sudoku.model.battle.BattleEntity

class BattleItemDiffCallback : DiffUtil.ItemCallback<BattleEntity>() {
    override fun areItemsTheSame(oldItem: BattleEntity, newItem: BattleEntity) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: BattleEntity, newItem: BattleEntity) =
        oldItem == newItem
}