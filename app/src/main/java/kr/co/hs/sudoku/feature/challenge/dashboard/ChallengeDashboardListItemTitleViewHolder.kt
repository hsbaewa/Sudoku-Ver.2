package kr.co.hs.sudoku.feature.challenge.dashboard

import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeTitleBinding

class ChallengeDashboardListItemTitleViewHolder<T : ChallengeDashboardListItem>(private val binding: LayoutListItemChallengeTitleBinding) :
    ChallengeDashboardListItemViewHolder<T>(binding.root) {
    override fun onBind(item: ChallengeDashboardListItem) {
        binding.tvTitle.text = itemView.context.getString(R.string.title_challenge)
    }

    override fun onViewAttachedToWindow() {}
    override fun onViewDetachedFromWindow() {}
    override fun onViewRecycled() {}
}