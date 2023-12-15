package kr.co.hs.sudoku.views

import android.content.Context
import androidx.recyclerview.widget.RecyclerView

class HorizontalDividerDecoration(context: Context) : DividerDecoration(context, HORIZONTAL) {

    private var spanCount = 0
    fun setSpanCount(count: Int): HorizontalDividerDecoration {
        this.spanCount = count
        return this
    }

    override fun shouldDrawDivider(position: Int, adapter: RecyclerView.Adapter<*>?): Boolean {
        return if (orientation == HORIZONTAL) {
            spanCount.takeIf { it > 0 }
                ?.run { (position + 1) % this != 0 }
                ?: true
        } else true
    }
}