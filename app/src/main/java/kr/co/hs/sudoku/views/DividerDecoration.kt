package kr.co.hs.sudoku.views

import android.content.Context
import com.google.android.material.divider.MaterialDividerItemDecoration
import kr.co.hs.sudoku.extension.platform.ContextExtension.getColorCompat

abstract class DividerDecoration(val context: Context, orientation: Int) :
    MaterialDividerItemDecoration(context, orientation) {

    fun transparent(): DividerDecoration {
        dividerColor = context.getColorCompat(android.R.color.transparent)
        return this
    }

    fun color(colorResId: Int): DividerDecoration {
        dividerColor = context.getColorCompat(colorResId)
        return this
    }

    fun thickness(size: Int): DividerDecoration {
        dividerThickness = size
        return this
    }

    fun thickness(size: Float): DividerDecoration {
        dividerThickness = size.toInt()
        return this
    }
}