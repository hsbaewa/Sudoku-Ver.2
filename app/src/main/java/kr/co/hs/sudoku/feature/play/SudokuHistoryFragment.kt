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
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.model.stage.history.HistoryItem
import kr.co.hs.sudoku.viewmodel.RecordViewModel
import kr.co.hs.sudoku.viewmodel.GamePlayViewModel
import kr.co.hs.sudoku.views.SudokuBoardView

class SudokuHistoryFragment : Fragment() {
    companion object {
        fun newInstance() = SudokuHistoryFragment()
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
                gamePlayViewModel.start()
            }
            gamePlayViewModel.statusFlow.collect {
                when (it) {
                    is GamePlayViewModel.Status.OnReady -> it.setupUIForReady()
                    is GamePlayViewModel.Status.OnStart -> it.setupUIForStart()
                    is GamePlayViewModel.Status.ChangedCell -> it.onChangedCell()
                    is GamePlayViewModel.Status.ToCorrect -> it.onToCorrect()
                    is GamePlayViewModel.Status.ToError -> it.onToError()
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

    private val gamePlayViewModel: GamePlayViewModel by lazy { sudokuStageViewModels() }

    private fun GamePlayViewModel.Status.OnReady.setupUIForReady() {
        binding.sudokuBoard.run {
            readySudoku(this@setupUIForReady.matrix)
            clearAllCellValue(this@setupUIForReady.matrix)
        }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 게임 준비
     * @param matrix
     **/
    private fun SudokuBoardView.readySudoku(matrix: IntMatrix) {
        dismissProgressIndicator()
        setupUI(matrix)
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment 스도쿠 뷰 setup
     * @param matrix 스도쿠 뷰의 데이터 모델
     **/
    private fun SudokuBoardView.setupUI(matrix: IntMatrix) {
        setRowCount(matrix.rowCount, matrix)
        isVisible = true
        cellTouchDownListener = { _, _ -> false }
        cellValueChangedListener = { _, _, _ -> false }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 셀 내부의 값들을 초기화 함
     * @param matrix
     **/
    private fun SudokuBoardView.clearAllCellValue(matrix: IntMatrix) {
        (0 until matrix.rowCount).forEach { row ->
            (0 until matrix.columnCount).forEach { column ->
                setCellValue(row, column, 0)
            }
        }
    }

    private fun GamePlayViewModel.Status.OnStart.setupUIForStart() {
        binding.sudokuBoard.fillCellValue(stage)
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

    private fun GamePlayViewModel.Status.ChangedCell.onChangedCell() =
        binding.sudokuBoard.setCellValue(row, column, value ?: 0)


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 에러 셀 해제
     **/
    private fun GamePlayViewModel.Status.ToCorrect.onToCorrect() =
        set.mapNotNull { (it as? IntCoordinateCellEntity)?.run { Pair(row, column) } }
            .forEach { binding.sudokuBoard.setError(it.first, it.second, false) }


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 에러 셀로 변환
     **/
    private fun GamePlayViewModel.Status.ToError.onToError() =
        set.mapNotNull { (it as? IntCoordinateCellEntity)?.run { Pair(row, column) } }
            .forEach { binding.sudokuBoard.setError(it.first, it.second, true) }

    private val recordViewModel: RecordViewModel by lazy { recordViewModels() }

    private fun setCellValue(row: Int, column: Int, value: Int) {
        gamePlayViewModel[row, column] = value
    }
}