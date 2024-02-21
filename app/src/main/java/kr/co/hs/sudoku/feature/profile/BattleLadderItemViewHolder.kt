package kr.co.hs.sudoku.feature.profile

import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutListItemProfileBattleLadderBinding

class BattleLadderItemViewHolder(private val binding: LayoutListItemProfileBattleLadderBinding) :
    ProfileItemViewHolder(binding.root) {
    override fun onBind(item: ProfileItem?) {
        when (item) {
            is ProfileItem.BattleLadder -> onBindBattleLadderItem(item)
            else -> {}
        }
    }

    private fun onBindBattleLadderItem(item: ProfileItem.BattleLadder) {
        with(binding.tvBattleLadder) {
            text = context.getString(
                R.string.format_statistics_with_ranking,
                item.winCount,
                item.playCount - item.winCount,
                when (item.ranking) {
                    0L -> context.getString(R.string.rank_format_nan)
                    1L -> context.getString(R.string.rank_format_first)
                    2L -> context.getString(R.string.rank_format_second)
                    3L -> context.getString(R.string.rank_format_third)
                    else -> context.getString(R.string.rank_format, item.ranking)
                }
            )
        }
    }

    override fun onViewRecycled() {}
}