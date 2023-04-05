package kr.co.hs.sudoku.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.stage.MatrixRepository

class StageListViewModel(private val repository: MatrixRepository<IntMatrix>) : ViewModel() {

    private val _stageList = MutableLiveData<List<IntMatrix>>()
    val stageList: LiveData<List<IntMatrix>> by this::_stageList

    suspend fun doRequestStageList() {
        val list = withContext(Dispatchers.IO) { repository.getList() }
        _stageList.value = list
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: MatrixRepository<IntMatrix>) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return modelClass.takeIf { it.isAssignableFrom(StageListViewModel::class.java) }
                ?.run { StageListViewModel(repository) as T }
                ?: throw IllegalArgumentException("unKnown ViewModel class")
        }
    }
}