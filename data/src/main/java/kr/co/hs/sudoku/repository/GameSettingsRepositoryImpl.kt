package kr.co.hs.sudoku.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map
import kr.co.hs.sudoku.mapper.SettingsMapper.PREFERENCE_ENABLED_HAPTIC_FEEDBACK
import kr.co.hs.sudoku.mapper.SettingsMapper.getGameSettingsEntity
import kr.co.hs.sudoku.model.settings.GameSettingsEntity
import kr.co.hs.sudoku.repository.settings.GameSettingsRepository

class GameSettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : GameSettingsRepository {

    override fun getGameSettings() = dataStore.data.map { it.getGameSettingsEntity() }

    override suspend fun setGameSettings(entity: GameSettingsEntity) {
        dataStore.edit {
            it[PREFERENCE_ENABLED_HAPTIC_FEEDBACK] = entity.enabledHapticFeedback
        }
    }
}