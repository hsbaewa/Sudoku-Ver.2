package kr.co.hs.sudoku.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.model.settings.GameSettingsEntity
import kr.co.hs.sudoku.repository.settings.GameSettingsRepository

class GameSettingsViewModel(private val repository: GameSettingsRepository) : ViewModel() {
    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: GameSettingsRepository) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return modelClass.takeIf { it.isAssignableFrom(GameSettingsViewModel::class.java) }
                ?.run { GameSettingsViewModel(repository) as T }
                ?: throw IllegalArgumentException("unKnown ViewModel class")
        }
    }

    val gameSettings: LiveData<GameSettingsEntity> = repository.getGameSettings().asLiveData()
    fun setGameSettings(entity: GameSettingsEntity) =
        viewModelScope.launch { repository.setGameSettings(entity) }
}