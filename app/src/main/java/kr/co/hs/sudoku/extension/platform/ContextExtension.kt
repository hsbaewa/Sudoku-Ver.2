package kr.co.hs.sudoku.extension.platform

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.preferencesDataStore

object ContextExtension {
    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- Preference ----------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private const val USER_PREFERENCES_NAME = "sudoku.preferences"
    val Context.dataStore by preferencesDataStore(name = USER_PREFERENCES_NAME)

    fun Context.getColorCompat(color: Int) = ContextCompat.getColor(this, color)
    fun Context.getDrawableCompat(resId: Int) = ContextCompat.getDrawable(this, resId)
}