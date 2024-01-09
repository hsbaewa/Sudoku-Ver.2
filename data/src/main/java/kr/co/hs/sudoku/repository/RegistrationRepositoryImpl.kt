package kr.co.hs.sudoku.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kr.co.hs.sudoku.mapper.SettingsMapper.PREFERENCE_HAS_SEEN_CHALLENGE_GUIDE
import kr.co.hs.sudoku.mapper.SettingsMapper.PREFERENCE_HAS_SEEN_MULTI_PLAY_GUIDE
import kr.co.hs.sudoku.mapper.SettingsMapper.PREFERENCE_HAS_SEEN_MULTI_PLAY_PARTICIPANT_NOTIFICATION
import kr.co.hs.sudoku.mapper.SettingsMapper.PREFERENCE_HAS_SEEN_SINGLE_PLAY_GUIDE
import kr.co.hs.sudoku.mapper.SettingsMapper.PREFERENCE_IS_FIRST_APP_OPEN
import kr.co.hs.sudoku.mapper.SettingsMapper.getRegistrationEntity
import kr.co.hs.sudoku.repository.settings.RegistrationRepository

class RegistrationRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : RegistrationRepository {

    private fun getRegistrationFlow() = dataStore.data.map { it.getRegistrationEntity() }

    override suspend fun isFirstAppOpened() =
        getRegistrationFlow().firstOrNull()?.isFirstAppOpen ?: true

    override suspend fun appOpened() {
        dataStore.edit { it[PREFERENCE_IS_FIRST_APP_OPEN] = false }
    }

    override suspend fun hasSeenSinglePlayGuide() =
        getRegistrationFlow().firstOrNull()?.hasSeenSinglePlayGuide ?: false

    override suspend fun seenSinglePlayGuide() {
        dataStore.edit { it[PREFERENCE_HAS_SEEN_SINGLE_PLAY_GUIDE] = true }
    }

    override suspend fun hasSeenMultiPlayGuide() =
        getRegistrationFlow().firstOrNull()?.hasSeenMultiPlayGuide ?: false

    override suspend fun seenMultiPlayGuide() {
        dataStore.edit { it[PREFERENCE_HAS_SEEN_MULTI_PLAY_GUIDE] = true }
    }

    override suspend fun hasSeenChallengeGuide() =
        getRegistrationFlow().firstOrNull()?.hasSeenChallengeGuide ?: false

    override suspend fun seenChallengeGuide() {
        dataStore.edit { it[PREFERENCE_HAS_SEEN_CHALLENGE_GUIDE] = true }
    }

    override suspend fun hasSeenNotificationParticipate() =
        getRegistrationFlow().firstOrNull()?.hasSeenMultiPlayParticipateNotification ?: false

    override suspend fun seenNotificationParticipate() {
        dataStore.edit { it[PREFERENCE_HAS_SEEN_MULTI_PLAY_PARTICIPANT_NOTIFICATION] = true }
    }

    override suspend fun clear() {
        dataStore.edit {
            it.remove(PREFERENCE_HAS_SEEN_SINGLE_PLAY_GUIDE)
            it.remove(PREFERENCE_HAS_SEEN_MULTI_PLAY_GUIDE)
            it.remove(PREFERENCE_HAS_SEEN_CHALLENGE_GUIDE)
            it.remove(PREFERENCE_HAS_SEEN_MULTI_PLAY_PARTICIPANT_NOTIFICATION)
        }
    }
}