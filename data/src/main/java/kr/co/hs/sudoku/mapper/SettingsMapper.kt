package kr.co.hs.sudoku.mapper

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import kr.co.hs.sudoku.model.settings.GameSettingsEntity
import kr.co.hs.sudoku.model.settings.RegistrationEntity

object SettingsMapper {
    val PREFERENCE_ENABLED_HAPTIC_FEEDBACK =
        booleanPreferencesKey("preference.enabledHapticFeedback")
    val PREFERENCE_IS_FIRST_APP_OPEN =
        booleanPreferencesKey("preference.isFirstAppOpen")
    val PREFERENCE_HAS_SEEN_SINGLE_PLAY_GUIDE =
        booleanPreferencesKey("preference.hasSeenSinglePlayGuide")
    val PREFERENCE_HAS_SEEN_MULTI_PLAY_GUIDE =
        booleanPreferencesKey("preference.hasSeenMultiPlayGuide")
    val PREFERENCE_HAS_SEEN_CHALLENGE_GUIDE =
        booleanPreferencesKey("preference.hasSeenChallengeGuide")

    fun Preferences.getGameSettingsEntity() =
        GameSettingsEntity(this[PREFERENCE_ENABLED_HAPTIC_FEEDBACK] ?: true)

    fun Preferences.getRegistrationEntity() = RegistrationEntity(
        this[PREFERENCE_IS_FIRST_APP_OPEN] ?: true,
        this[PREFERENCE_HAS_SEEN_SINGLE_PLAY_GUIDE] ?: false,
        this[PREFERENCE_HAS_SEEN_MULTI_PLAY_GUIDE] ?: false,
        this[PREFERENCE_HAS_SEEN_CHALLENGE_GUIDE] ?: false
    )
}