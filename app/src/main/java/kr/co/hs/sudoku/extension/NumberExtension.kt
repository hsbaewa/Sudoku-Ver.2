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

    fun Long.toTimerFormat() = this.let { allMillis ->
        val millis = allMillis % 1000
        val allSeconds = allMillis / 1000
        val seconds = allSeconds % 60
        val allMinutes = allSeconds / 60
        val minutes = allMinutes % 60
        val hour = allMinutes / 60
        String.format("%d:%02d:%02d.%03d", hour, minutes, seconds, millis)
    }
}