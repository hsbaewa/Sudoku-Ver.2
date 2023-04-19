package kr.co.hs.sudoku.feature.challenge

import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutItemRankBinding
import kr.co.hs.sudoku.model.rank.RankerEntity

class RankForMineViewHolder(binding: LayoutItemRankBinding) : RankViewHolder(binding) {
    override fun RankerEntity.getFormattedName() =
        itemView.context.getString(R.string.rank_mine_format_name, displayName)
}