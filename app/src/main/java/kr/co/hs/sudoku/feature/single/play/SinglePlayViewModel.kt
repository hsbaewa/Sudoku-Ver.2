package kr.co.hs.sudoku.feature.single.play

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.usecase.AutoGenerateSudokuUseCase
import kr.co.hs.sudoku.viewmodel.ViewModel
import javax.inject.Inject

class SinglePlayViewModel
@Inject constructor(
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

    private val _command = MutableLiveData<Command>(Matrix(matrix))
    val command: LiveData<Command> by this::_command
    private var lastCreatedStage: List<List<Int>>? = null

    fun create() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val stage = withContext(Dispatchers.IO) {
            val useCase = AutoGenerateSudokuUseCase(matrix.boxSize, matrix.boxCount, matrix)
            useCase().last()
        }
        val table = stage.toValueTable()
        this@SinglePlayViewModel.lastCreatedStage = table
        _command.value = Created(table)
        setProgress(false)
    }

    fun start() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        lastCreatedStage?.run { _command.value = Started(this) }
    }

    fun initMatrix() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        _command.value = Matrix(matrix)
    }

    fun startReplay() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        _command.value = StartReplay
    }


    sealed interface Command
    data class Matrix(val matrix: IntMatrix) : Command
    data class Created(val stage: List<List<Int>>) : Command
    data class Started(val stage: List<List<Int>>) : Command
    object StartReplay : Command
}