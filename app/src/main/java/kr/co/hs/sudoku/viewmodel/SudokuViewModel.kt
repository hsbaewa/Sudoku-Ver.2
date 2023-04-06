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

class SudokuViewModel(private val repository: MatrixRepository<IntMatrix>) : ViewModel() {
    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: MatrixRepository<IntMatrix>) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return modelClass.takeIf { it.isAssignableFrom(SudokuViewModel::class.java) }
                ?.run { SudokuViewModel(repository) as T }
                ?: throw IllegalArgumentException("unKnown ViewModel class")
        }
    }


    private val _matrixList = MutableLiveData<List<IntMatrix>>()
    val matrixList: LiveData<List<IntMatrix>> by this::_matrixList

    fun requestStageList() = viewModelScope.launch {
        val list = withContext(Dispatchers.IO) { repository.getList() }
        _matrixList.value = list
    }

    private val _sudoku = MutableLiveData<Stage>()
    val sudoku: LiveData<Stage> by this::_sudoku

    fun loadStage(level: Int) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            matrixList.value?.get(level).takeIf { it != null }
                ?.let { AutoGenerateSudokuUseCase(it.boxSize, it.boxCount, it) }
                ?.let { it().first() }
        }?.let { _sudoku.value = it }
    }
}