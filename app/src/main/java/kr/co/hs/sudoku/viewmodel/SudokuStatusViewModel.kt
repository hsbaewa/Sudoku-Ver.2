package kr.co.hs.sudoku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.CellEntity
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.usecase.BuildSudokuUseCaseImpl

open class SudokuStatusViewModel : ViewModel(), IntCoordinateCellEntity.ValueChangedListener {
    private fun initMatrix(matrix: IntMatrix) {
        viewModelScope.launch {
            val useCase = BuildSudokuUseCaseImpl(matrix)
            useCase().first().bind()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    protected fun Stage.bind() {
        addValueChangedListener(this@SudokuStatusViewModel)
        this@SudokuStatusViewModel.sudoku = this
        startingMatrix = CustomMatrix(this.toValueTable())
        statusFlow.resetReplayCache()
        viewModelScope.launch { statusFlow.emit(Status.OnReady(this@bind)) }
    }

    lateinit var startingMatrix: IntMatrix
    fun backToStartingMatrix() = initMatrix(startingMatrix)

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 스도쿠 게임 동작의 flow
     **/
    val statusFlow = MutableSharedFlow<Status>(replay = 10)

    protected lateinit var sudoku: Stage

    override fun onChanged(cell: IntCoordinateCellEntity) {
        val errorCell = sudoku.getDuplicatedCells().toList().toHashSet()
        if (errorCell != lastErrorCell) {
            lastErrorCell.subtract(errorCell)
                .takeIf { it.isNotEmpty() }
                ?.let { viewModelScope.launch { statusFlow.emit(Status.ToCorrect(it)) } }

            errorCell.subtract(lastErrorCell)
                .takeIf { it.isNotEmpty() }
                ?.let { viewModelScope.launch { statusFlow.emit(Status.ToError(it)) } }

            lastErrorCell = errorCell
        }

        viewModelScope.launch {
            val value = if (cell.isEmpty()) null else cell.getValue()
            statusFlow.emit(Status.ChangedCell(cell.row, cell.column, value))
            isCompleted().takeIf { it }?.run {
                statusFlow.emit(Status.Completed)
            }
        }
    }


    private var lastErrorCell = HashSet<CellEntity<Int>>()


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 스도쿠 게임 상태의 대한 정보인 seald interface
     **/
    sealed interface Status {
        data class OnReady(val stage: Stage) : Status
        data class OnStart(val stage: Stage) : Status
        data class ToCorrect(val set: Set<CellEntity<Int>>) : Status
        data class ToError(val set: Set<CellEntity<Int>>) : Status
        object Completed : Status
        data class ChangedCell(val row: Int, val column: Int, val value: Int?) : Status
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
        takeIf { this::sudoku.isInitialized }?.run { sudoku.removeValueChangedListener(this@SudokuStatusViewModel) }
    }

    fun start() = takeIf { this::sudoku.isInitialized }
        ?.let {
            viewModelScope.launch { statusFlow.emit(Status.OnStart(stage = sudoku)) }
        }

    fun isCompleted() = sudoku.isCompleted()

    fun getStage() = sudoku
}