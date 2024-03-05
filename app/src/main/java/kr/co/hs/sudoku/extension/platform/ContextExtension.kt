package kr.co.hs.sudoku.extension.platform

import android.content.Context
import android.content.pm.PackageManager.GET_META_DATA
import androidx.core.content.ContextCompat

object ContextExtension {
    fun Context.getColorCompat(color: Int) = ContextCompat.getColor(this, color)
    fun Context.getDrawableCompat(resId: Int) = ContextCompat.getDrawable(this, resId)


    fun Context.getMetaData(key: String) = packageManager
        .runCatching { getApplicationInfo(packageName, GET_META_DATA).metaData.getString(key) }
        .getOrNull()
}