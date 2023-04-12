package kr.co.hs.sudoku.extension

import android.content.res.Resources
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.applyDimension

object NumberExtension {
    val Int.toPx
        get() = toFloat().dpToPx().toInt()

    val Float.toPx
        get() = dpToPx()

    private fun Float.dpToPx() =
        applyDimension(COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)
}