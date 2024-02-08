package kr.co.hs.sudoku.feature.stage

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dataStore
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.matrix.EmptyMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.CellValueEntity
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.model.stage.impl.IntCellEntityImpl
import kr.co.hs.sudoku.model.stage.impl.IntCoordinate
import kr.co.hs.sudoku.model.stage.impl.IntCoordinateCellEntityImpl
import kr.co.hs.sudoku.model.stage.impl.MutableStageImpl
import kr.co.hs.sudoku.repository.GameSettingsRepositoryImpl
import kr.co.hs.sudoku.viewmodel.GameSettingsViewModel
import kr.co.hs.sudoku.viewmodel.RecordViewModel
import kr.co.hs.sudoku.views.SudokuView
import kotlin.math.sqrt

abstract class StageFragment : Fragment() {
    companion object {
        private const val EXTRA_FIX_CELL = "EXTRA_FIX_CELL"

        inline fun <reified T : StageFragment> newInstance(matrix: IntMatrix): T =
            T::class.java.getDeclaredConstructor().newInstance()
                .apply { arguments = newInstanceArguments(matrix) }

        fun newInstanceArguments(matrix: IntMatrix) = bundleOf(
            EXTRA_FIX_CELL to matrix.flatten().toIntArray()
        )
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


    abstract val board: SudokuView
    abstract val silhouette: View

    private val gameSettingsViewModel: GameSettingsViewModel by viewModels {
        GameSettingsViewModel.Factory(GameSettingsRepositoryImpl(dataStore))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(board) {
            initFixCell()
            setOnCellValueChangedListener(object : SudokuView.CellValueChangedListener {
                override fun onChangedCell(row: Int, column: Int, value: Int?) {
                    onCellValueChangedListener(row, column, value)
                }

            })
        }

        valueChangedListener?.run { stage.addValueChangedListener(this) }
        gameSettingsViewModel.gameSettings.observe(viewLifecycleOwner) {
            board.enabledHapticFeedback = it.enabledHapticFeedback
        }
    }


    /**
     * sudoku board 관련
     */
    private fun SudokuView.initFixCell() {
        setFixedCellValues(fixCell)
    }

    // stage 정보를 뷰에 적용
    private fun SudokuView.setStage(stage: Stage) = with(stage) {
        List(rowCount * columnCount) {
            it.div(rowCount) to it.rem(columnCount)
        }.forEach {
            runCatching { get(it.first, it.second) }.getOrDefault(0)
                .takeIf { it > 0 }
                ?.run {
                    setCellValue(it.first, it.second, this)
                }
        }
        isVisibleNumber = true
        invalidate()
    }

    // 오류 셀을 뷰에 보여줌
    private fun SudokuView.showError(stage: Stage) = with(stage) {
        val errorValues = List(stage.rowCount) { MutableList(stage.columnCount) { false } }
        val currentError = getDuplicatedCells().toList().toSet()
        currentError.forEach {
            with(it as IntCoordinateCellEntity) { errorValues[row][column] = true }
        }
        setError(errorValues)
    }

    // 특정 셀을 view에 채움
    private fun SudokuView.setValue(row: Int, column: Int, value: Int) =
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

    // mutable 값을 모두 삭제
    fun clearBoard() {
        board.initFixCell()
        stage.clearAllMutableValue()
        setValues(stage.toValueTable())
    }

    private fun Stage.clearAllMutableValue() {
        valueChangedListener?.run { removeValueChangedListener(this) }
        fixCell.forEachIndexed { row, ints ->
            ints.forEachIndexed { column, value ->
                val cell = getCell(row, column)
                when {
                    value > 0 -> cell.toImmutable(cell.getValue())
                    else -> cell.toEmpty()
                }
            }
        }
        valueChangedListener?.run { addValueChangedListener(this) }
    }

    // 보드 초기화
    fun initBoard() {
        stage.clearAllMutableValue()
        board.initFixCell()
    }
}