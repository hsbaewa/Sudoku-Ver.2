package kr.co.hs.sudoku.feature.play

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutPlayAutoBinding
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.model.stage.history.HistoryItem
import kr.co.hs.sudoku.viewmodel.RecordViewModel
import kr.co.hs.sudoku.viewmodel.SudokuStageViewModel
import kr.co.hs.sudoku.viewmodel.SudokuStatusViewModel
import kr.co.hs.sudoku.views.SudokuBoardView

class ReplayFragment : Fragment() {
    companion object {
        fun new() = ReplayFragment()
    }

    lateinit var binding: LayoutPlayAutoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = LayoutPlayAutoBinding.inflate(inflater, container, false).also {
        binding = it
        it.lifecycleOwner = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            withStarted {
                binding.sudokuBoard.setupUIForStart(sudokuStageViewModel.getStage())
                recordViewModel.startLog()
            }
            sudokuStageViewModel.statusFlow.collect {
                when (it) {
                    is SudokuStatusViewModel.Status.ChangedCell -> it.onChangedCell()
                    is SudokuStatusViewModel.Status.ToCorrect -> it.onToCorrect()
                    is SudokuStatusViewModel.Status.ToError -> it.onToError()
                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            recordViewModel.cellEventHistoryFlow.collect {
                when (it) {
                    is HistoryItem.Removed -> setCellValue(it.row, it.column, 0)
                    is HistoryItem.Set -> setCellValue(it.row, it.column, it.value)
                }
            }
        }
    }

    private val sudokuStageViewModel: SudokuStageViewModel by lazy { sudokuStageViewModels() }


    private fun SudokuBoardView.setupUIForStart(stage: Stage) {
        setRowCount(stage.rowCount, stage.toValueTable())
        fillCellValue(stage)
        isVisible = true
        cellTouchDownListener = { _, _ -> false }
        cellValueChangedListener = { _, _, _ -> false }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 셀 내부의 값들을 초기화 함
     * @param stage
     **/
    private fun SudokuBoardView.fillCellValue(stage: Stage) {
        (0 until stage.rowCount).forEach { row ->
            (0 until stage.columnCount).forEach { column ->
                runCatching { stage[row, column] }.getOrNull()
                    ?.takeIf { it > 0 }
                    ?.run { setCellValue(row, column, this) }
            }
        }
    }

    private fun SudokuStatusViewModel.Status.ChangedCell.onChangedCell() =
        binding.sudokuBoard.setCellValue(row, column, value ?: 0)


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 에러 셀 해제
     **/
    private fun SudokuStatusViewModel.Status.ToCorrect.onToCorrect() =
        set.mapNotNull { (it as? IntCoordinateCellEntity)?.run { Pair(row, column) } }
            .forEach { binding.sudokuBoard.setError(it.first, it.second, false) }


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 에러 셀로 변환
     **/
    private fun SudokuStatusViewModel.Status.ToError.onToError() =
        set.mapNotNull { (it as? IntCoordinateCellEntity)?.run { Pair(row, column) } }
            .forEach { binding.sudokuBoard.setError(it.first, it.second, true) }

    private val recordViewModel: RecordViewModel by lazy { recordViewModels() }

    private fun setCellValue(row: Int, column: Int, value: Int) {
        sudokuStageViewModel[row, column] = value
    }
}