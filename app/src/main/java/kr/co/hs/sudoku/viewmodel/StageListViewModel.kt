package kr.co.hs.sudoku.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.repository.stage.MatrixRepository
import kr.co.hs.sudoku.usecase.AutoGenerateSudokuUseCase

class StageListViewModel(private val repository: MatrixRepository<IntMatrix>) : ViewModel() {
    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: MatrixRepository<IntMatrix>) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return modelClass.takeIf { it.isAssignableFrom(StageListViewModel::class.java) }
                ?.run { StageListViewModel(repository) as T }
                ?: throw IllegalArgumentException("unKnown ViewModel class")
        }
    }


    private val _stageList = MutableLiveData<List<IntMatrix>>()
    val stageList: LiveData<List<IntMatrix>> by this::_stageList

    suspend fun doRequestStageList() {
        val list = withContext(Dispatchers.IO) { repository.getList() }
        _stageList.value = list
    }

    fun getMatrix(level: Int) = stageList.value?.get(level)

    private val _stage = MutableLiveData<Stage>()
    val stage: LiveData<Stage> by this::_stage

    fun loadStage(level: Int) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            getMatrix(level).takeIf { it != null }
                ?.let { AutoGenerateSudokuUseCase(it.boxSize, it.boxCount, it) }
                ?.let { it().first() }
        }?.let { _stage.value = it }
    }
}