package kr.co.hs.sudoku.feature.battle

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.model.battle.ParticipantEntity
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.matrix.EmptyMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.CellEntity
import kr.co.hs.sudoku.model.stage.CellValueEntity
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.model.stage.impl.IntCellEntityImpl
import kr.co.hs.sudoku.model.stage.impl.IntCoordinate
import kr.co.hs.sudoku.model.stage.impl.IntCoordinateCellEntityImpl
import kr.co.hs.sudoku.model.stage.impl.MutableStageImpl
import kr.co.hs.sudoku.viewmodel.RecordViewModel
import kr.co.hs.sudoku.views.SudokuBoardView
import kotlin.math.sqrt

abstract class BoardFragment : Fragment() {
    companion object {
        const val EXTRA_FIX_CELL = "EXTRA_FIX_CELL"
    }

    // 결정된 고정 셀
    private val fixCell: IntMatrix by lazy {
        arguments
            ?.getIntArray(EXTRA_FIX_CELL)
            ?.run {
                val rowSize = sqrt(this.size.toFloat()).toInt()
                List(rowSize) { row ->
                    List(rowSize) { column ->
                        this[row.times(rowSize) + column]
                    }
                }.run { CustomMatrix(this) }
            }
            ?: EmptyMatrix()
    }


    abstract val board: SudokuBoardView
    abstract val silhouette: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(board) {
            initFixCell()
            cellTouchDownListener = { row, column -> onCellTouchDown(row, column) }
            cellValueChangedListener =
                { row, column, value -> onCellValueChangedListener(row, column, value) }
        }

        valueChangedListener?.run { stage.addValueChangedListener(this) }
    }


    /**
     * sudoku board 관련
     */
    private fun SudokuBoardView.initFixCell() = setRowCount(fixCell.size, fixCell)

    // stage 정보를 뷰에 적용
    private fun SudokuBoardView.setStage(stage: Stage) = with(stage) {
        List(rowCount * columnCount) {
            it.div(rowCount) to it.rem(columnCount)
        }.forEach {
            runCatching { get(it.first, it.second) }.getOrDefault(0)
                .takeIf { it > 0 }
                ?.run { setCellValue(it.first, it.second, this) }
        }

        showError(this)
    }

    // 마지막으로 알고 있던 오류 셀 정보
    private var lastKnownErrorCell: Set<CellEntity<Int>>? = null

    // 오류 셀을 뷰에 보여줌
    private fun SudokuBoardView.showError(stage: Stage) = with(stage) {
        val currentError = getDuplicatedCells().toList().toSet()
        val lastError = lastKnownErrorCell ?: emptySet()
        if (currentError != lastError) {
            lastError.takeIf { it.isNotEmpty() }
                ?.let { errorToCorrect(it, currentError) }

            currentError.takeIf { it.isNotEmpty() }
                ?.let { correctToError(lastError, it) }
        }

        lastKnownErrorCell = currentError
    }

    // 이전에 error 였는데 지금은 correct인거 찾기
    private fun SudokuBoardView.errorToCorrect(
        last: Set<CellEntity<Int>>,
        current: Set<CellEntity<Int>>
    ) = last.subtract(current)
        .mapNotNull { it as? IntCoordinateCellEntity }
        .forEach { setError(it.row, it.column, false) }

    // 이전에 correct었는데 지금은 error인거 찾기
    private fun SudokuBoardView.correctToError(
        last: Set<CellEntity<Int>>,
        current: Set<CellEntity<Int>>
    ) = current.subtract(last)
        .mapNotNull { it as? IntCoordinateCellEntity }
        .forEach { setError(it.row, it.column, true) }

    // 특정 셀을 view에 채움
    private fun SudokuBoardView.setValue(row: Int, column: Int, value: Int) =
        setCellValue(row, column, value)

    abstract fun onCellTouchDown(row: Int, column: Int): Boolean
    abstract fun onCellValueChangedListener(row: Int, column: Int, value: Int?): Boolean

    // 활성화 여부
    protected var enabled: Boolean? = null
        set(value) {
            if (field == value)
                return

            silhouette.isVisible = value == false
            field = value
        }


    private val stage: Stage by lazy {
        with(fixCell) {
            List(rowCount) { row ->
                MutableList<IntCoordinateCellEntity>(columnCount) { column ->
                    val cellEntity = IntCellEntityImpl(
                        this[row][column].let {
                            when {
                                it > 0 -> CellValueEntity.Immutable(it)
                                else -> CellValueEntity.Empty
                            }
                        }
                    )
                    IntCoordinateCellEntityImpl(IntCoordinate(row, column), cellEntity)
                }
            }.run { MutableStageImpl(boxSize, boxCount, this) }
        }
    }


    abstract fun setStatus(participant: ParticipantEntity)


    // 전달 받은 matrix를 stage에 적용 후 view에 적용하는 함수
    fun setValues(matrix: List<List<Int>>) {
        viewLifecycleOwner.lifecycleScope.launch {
            withStarted {


                with(stage) {
                    matrix.indices.forEach { row ->
                        matrix[row].indices.forEach { column ->

                            matrix[row][column]
                                .takeIf { it > 0 }
                                ?.let { matrixValue ->
                                    runCatching { getCell(row, column) }.getOrNull()
                                        ?.takeIf {
                                            val stageValue =
                                                it.runCatching { getValue() }.getOrDefault(0)
                                            stageValue != matrixValue
                                        }
                                        ?.run {
                                            when {
                                                isMutable() -> setValue(matrixValue)
                                                isImmutable() -> toImmutable(matrixValue)
                                                isEmpty() -> toMutable(matrixValue)
                                            }
                                        }
                                }

                        }
                    }
                }
                board.setStage(stage)


            }
        }


    }

    // 전달 받은 값을 stage에 저장하고 view에 적용
    fun setValue(row: Int, column: Int, value: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            withStarted {
                stage[row, column] = value
                board.setValue(row, column, value)
                board.showError(stage)
            }
        }
    }

    fun isCleared() = stage.isSudokuClear()
    fun getClearTime() = stage.getClearTime()

    private var valueChangedListener: IntCoordinateCellEntity.ValueChangedListener? = null
    fun setValueChangedListener(l: IntCoordinateCellEntity.ValueChangedListener?) {
        this.valueChangedListener?.run { stage.removeValueChangedListener(this) }
        l?.run { stage.addValueChangedListener(l) }
        this.valueChangedListener = l
    }

    fun bindStage(recordViewModel: RecordViewModel) {
        recordViewModel.bind(stage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        valueChangedListener?.run { stage.removeValueChangedListener(this) }
    }
}