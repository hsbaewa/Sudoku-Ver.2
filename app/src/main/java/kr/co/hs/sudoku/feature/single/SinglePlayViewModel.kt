package kr.co.hs.sudoku.feature.single

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.usecase.AutoGenerateSudokuUseCase
import kr.co.hs.sudoku.viewmodel.ViewModel

class SinglePlayViewModel(
    val matrix: IntMatrix
) : ViewModel() {
    class ProviderFactory(private val matrix: IntMatrix) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(SinglePlayViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                SinglePlayViewModel(matrix) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }

    private val _stage = MutableLiveData<Stage?>()
    val stage: LiveData<Stage?> by this::_stage

    fun createStage() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val stage = withContext(Dispatchers.IO) {
            val useCase = AutoGenerateSudokuUseCase(matrix.boxSize, matrix.boxCount, matrix)
            useCase().last()
        }
        _stage.value = stage
        setProgress(false)
    }


    private val _command = MutableLiveData<Command>(Matrix(matrix))
    val command: LiveData<Command> by this::_command

    fun create() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val stage = withContext(Dispatchers.IO) {
            val useCase = AutoGenerateSudokuUseCase(matrix.boxSize, matrix.boxCount, matrix)
            useCase().last()
        }
        _command.value = Created(stage.toValueTable())
        setProgress(false)
    }

    fun start() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        _command.value = Started
    }

    fun recreate() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val stage = withContext(Dispatchers.IO) {
            val useCase = AutoGenerateSudokuUseCase(matrix.boxSize, matrix.boxCount, matrix)
            useCase().last()
        }
        _command.value = Recreated(stage.toValueTable())
        setProgress(false)
    }

    fun replay() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        _command.value = StartReplay
    }


    sealed interface Command
    data class Matrix(val matrix: IntMatrix) : Command
    data class Created(val stage: List<List<Int>>) : Command
    object Started : Command
    data class Recreated(val stage: List<List<Int>>) : Command
    object StartReplay : Command
}