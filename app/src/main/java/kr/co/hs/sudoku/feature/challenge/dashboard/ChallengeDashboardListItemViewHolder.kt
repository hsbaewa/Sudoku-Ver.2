package kr.co.hs.sudoku.feature.challenge.dashboard

import android.view.View
import kr.co.hs.sudoku.core.ViewHolder

abstract class ChallengeDashboardListItemViewHolder<out T : ChallengeDashboardListItem>(itemView: View) :
    ViewHolder(itemView) {
    abstract fun onBind(item: ChallengeDashboardListItem)
    abstract fun onViewAttachedToWindow()
    abstract fun onViewDetachedFromWindow()
    abstract fun onViewRecycled()
}