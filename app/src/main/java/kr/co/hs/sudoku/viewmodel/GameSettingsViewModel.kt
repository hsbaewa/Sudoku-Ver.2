package kr.co.hs.sudoku.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.di.repositories.GameSettingsRepositoryQualifier
import kr.co.hs.sudoku.model.settings.GameSettingsEntity
import kr.co.hs.sudoku.repository.settings.GameSettingsRepository
import javax.inject.Inject

@HiltViewModel
class GameSettingsViewModel
@Inject constructor(
    @GameSettingsRepositoryQualifier
    private val repository: GameSettingsRepository
) : ViewModel() {

    val gameSettings: LiveData<GameSettingsEntity> = repository.getGameSettings().asLiveData()
    fun setGameSettings(entity: GameSettingsEntity) =
        viewModelScope.launch(viewModelScopeExceptionHandler) {
            withContext(Dispatchers.IO) { repository.setGameSettings(entity) }
        }
}