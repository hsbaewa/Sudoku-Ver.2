package kr.co.hs.sudoku.feature.matrixlist

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager

class MatrixListLayoutManager : GridLayoutManager {
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context?, spanCount: Int) : super(context, spanCount)
    constructor(
        context: Context?,
        spanCount: Int,
        orientation: Int,
        reverseLayout: Boolean
    ) : super(context, spanCount, orientation, reverseLayout)

    init {
        spanSizeLookup = MatrixListSpanSizeLookup()
    }

    fun setAdapter(adapter: MatrixListItemAdapter) {
        with(spanSizeLookup as MatrixListSpanSizeLookup) { this.adapter = adapter }
    }

    private inner class MatrixListSpanSizeLookup : SpanSizeLookup() {
        var adapter: MatrixListItemAdapter? = null

        override fun getSpanSize(position: Int) =
            when (adapter?.currentList?.get(position)) {
                is MatrixListItem.HeaderItem -> spanCount
                is MatrixListItem.MatrixItem -> 1
                is MatrixListItem.TitleItem -> spanCount
                else -> 1
            }
    }
}