package kr.co.hs.sudoku.extension.platform

import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener

object ViewExtension {
    inline fun View.observeMeasuredSize(crossinline onMeasuredSize: (width: Int, height: Int) -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                onMeasuredSize(measuredWidth, measuredHeight)
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }
}