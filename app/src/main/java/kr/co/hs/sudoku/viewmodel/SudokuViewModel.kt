package kr.co.hs.sudoku.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.CellEntity
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.repository.stage.MatrixRepository
import kr.co.hs.sudoku.usecase.AutoGenerateSudokuUseCase

class SudokuViewModel(private val repository: MatrixRepository<IntMatrix>) : ViewModel(),
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
            return modelClass.takeIf { it.isAssignableFrom(SudokuViewModel::class.java) }
                ?.run { SudokuViewModel(repository) as T }
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
        }
            ?.also { sudoku = it }
            ?.also { it.setValueChangedListener(this@SudokuViewModel) }
            ?.also { sudokuStatusFlow.value = SudokuStatus.OnReady(it) }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 스도쿠 게임 동작의 flow
     **/
    val sudokuStatusFlow = MutableStateFlow<SudokuStatus>(SudokuStatus.OnInit)

    private lateinit var sudoku: Stage

    override fun onChanged(cell: IntCoordinateCellEntity) {
        sudokuStatusFlow.value = SudokuStatus.ChangedCell(
            cell.row,
            cell.column,
            if (cell.isEmpty()) null else cell.getValue()
        )

        val errorCell = sudoku.getDuplicatedCells().toList().toHashSet()
        if (errorCell != lastErrorCell) {
            lastErrorCell.subtract(errorCell)
                .takeIf { it.isNotEmpty() }
                ?.run { sudokuStatusFlow.value = SudokuStatus.ToCorrect(this) }

            errorCell.subtract(lastErrorCell)
                .takeIf { it.isNotEmpty() }
                ?.run { sudokuStatusFlow.value = SudokuStatus.ToError(this) }

            lastErrorCell = errorCell
        }

        sudoku.isCompleted()
            .takeIf { it }
            ?.run { sudokuStatusFlow.value = SudokuStatus.Completed }
    }


    private var lastErrorCell = HashSet<CellEntity<Int>>()


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 스도쿠 게임 상태의 대한 정보인 seald interface
     **/
    sealed interface SudokuStatus {
        data class OnReady(val stage: Stage) : SudokuStatus
        object OnInit : SudokuStatus
        data class ToCorrect(val set: Set<CellEntity<Int>>) : SudokuStatus
        data class ToError(val set: Set<CellEntity<Int>>) : SudokuStatus
        object Completed : SudokuStatus
        data class ChangedCell(val row: Int, val column: Int, val value: Int?) : SudokuStatus
    }


    operator fun set(row: Int, column: Int, value: Int) =
        takeIf { this::sudoku.isInitialized }?.run { sudoku[row, column] = value }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment value가 변경 가능한 셀인지 여부
     * @param row
     * @param column
     * @return 변경 가능 여부
     **/
    fun isMutableCell(row: Int, column: Int) =
        takeIf { this::sudoku.isInitialized }
            ?.run { !sudoku.getCell(row, column).isImmutable() } ?: false

    override fun onCleared() {
        super.onCleared()
        takeIf { this::sudoku.isInitialized }?.run { sudoku.setValueChangedListener(null) }
    }
}