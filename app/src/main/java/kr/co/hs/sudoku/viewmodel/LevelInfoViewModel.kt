package kr.co.hs.sudoku.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.repository.stage.StageRepository

class LevelInfoViewModel(private val repository: StageRepository) : ViewModel() {

    private val _stageList = MutableLiveData<List<Stage>>()
    val stageList: LiveData<List<Stage>> by this::_stageList

    suspend fun doRequestStageList() {
        withContext(Dispatchers.IO) { repository.doRequestStageList() }
        _stageList.value = repository
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: StageRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return modelClass.takeIf { it.isAssignableFrom(LevelInfoViewModel::class.java) }
                ?.run { LevelInfoViewModel(repository) as T }
                ?: throw IllegalArgumentException("unKnown ViewModel class")
        }
    }
}