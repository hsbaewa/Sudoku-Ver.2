package kr.co.hs.sudoku.mapper

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import kr.co.hs.sudoku.model.settings.GameSettingsEntity

object SettingsMapper {
    val PREFERENCE_ENABLED_HAPTIC_FEEDBACK =
        booleanPreferencesKey("preference.enabledHapticFeedback")
    val PREFERENCE_IS_FIRST_APP_OPEN =
        booleanPreferencesKey("preference.isFirstAppOpen")

    fun Preferences.toDomain() =
        GameSettingsEntity(
            this[PREFERENCE_ENABLED_HAPTIC_FEEDBACK] ?: true,
            this[PREFERENCE_IS_FIRST_APP_OPEN] ?: true
        )
}