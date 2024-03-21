package kr.co.hs.sudoku.feature.single.play

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.usecase.SudokuGenerateUseCase
import kr.co.hs.sudoku.viewmodel.ViewModel
import javax.inject.Inject

class SinglePlayViewModel
@Inject constructor(
    val matrix: IntMatrix,
    private val sudokuGenerator: SudokuGenerateUseCase
) : ViewModel() {
    class ProviderFactory(
        private val matrix: IntMatrix,
        private val sudokuGenerator: SudokuGenerateUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(SinglePlayViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                SinglePlayViewModel(matrix, sudokuGenerator) as T
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
        val stage = sudokuGenerator(matrix, this)
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