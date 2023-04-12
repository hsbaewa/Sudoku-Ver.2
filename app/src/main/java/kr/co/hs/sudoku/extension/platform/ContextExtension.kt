package kr.co.hs.sudoku.extension.platform

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

object ContextExtension {
    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- Preference ----------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private const val USER_PREFERENCES_NAME = "sudoku.preferences"
    val Context.dataStore by preferencesDataStore(name = USER_PREFERENCES_NAME)
}