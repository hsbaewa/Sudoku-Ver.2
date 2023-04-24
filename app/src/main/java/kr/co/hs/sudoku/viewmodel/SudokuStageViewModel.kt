package kr.co.hs.sudoku.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.*
import kr.co.hs.sudoku.repository.stage.MatrixRepository
import kr.co.hs.sudoku.usecase.AutoGenerateSudokuUseCase

class SudokuStageViewModel(private val repository: MatrixRepository<IntMatrix>) :
    SudokuStatusViewModel(),
    IntCoordinateCellEntity.ValueChangedListener {

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment ViewModelProvider를 위한 Factory
     * @param repository
     **/
    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: MatrixRepository<IntMatrix>) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return modelClass.takeIf { it.isAssignableFrom(SudokuStageViewModel::class.java) }
                ?.run { SudokuStageViewModel(repository) as T }
                ?: throw IllegalArgumentException("unKnown ViewModel class")
        }
    }


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment Sudoku Matrix 정보, 이를 통해 Stage를 빌드 할 수 있다.
     * @return
     **/
    private val _matrixList = MutableLiveData<List<IntMatrix>>()
    val matrixList: LiveData<List<IntMatrix>> by this::_matrixList

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment Sudoku Matrix 정보 로드
     **/
    fun requestMatrix() = viewModelScope.launch {
        val list = withContext(Dispatchers.IO) { repository.getList() }
        _matrixList.value = list
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment Matrix 정보를 통해 스도쿠를 로드함, 먼저 requestMatrix()함수가 선행으로 호출 되어야 함.
     * @param level 레벨
     **/
    fun loadStage(level: Int) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            matrixList.value?.get(level).takeIf { it != null }
                ?.let { AutoGenerateSudokuUseCase(it.boxSize, it.boxCount, it) }
                ?.let { it().first() }
        }?.bind()
    }
}