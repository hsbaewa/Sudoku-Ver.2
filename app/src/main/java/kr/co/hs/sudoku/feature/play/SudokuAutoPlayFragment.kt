package kr.co.hs.sudoku.feature.play

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutPlayAutoBinding
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.viewmodel.GamePlayViewModel
import kr.co.hs.sudoku.views.SudokuBoardView

class SudokuAutoPlayFragment : Fragment() {

    companion object {
        fun new(matrix: IntMatrix) = SudokuAutoPlayFragment().apply {
            arguments = Bundle().apply {
                putSudokuMatrixToExtra(matrix)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = with(LayoutPlayAutoBinding.inflate(inflater, container, false)) {
        binding = this
        lifecycleOwner = this@SudokuAutoPlayFragment
        root
    }

    private lateinit var binding: LayoutPlayAutoBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            gamePlayViewModel.statusFlow.collect {
                when (it) {
                    is GamePlayViewModel.Status.OnStart -> onOtherSudokuStart()
                    is GamePlayViewModel.Status.Completed -> onOtherSudokuCompleted()
                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            withStarted {
                getSudokuMatrixFromExtra()?.run { localGamePlayViewModel.setSudokuMatrix(this) }
            }

            localGamePlayViewModel.statusFlow.collect {
                when (it) {
                    is GamePlayViewModel.Status.OnReady -> it.setupUIForReady()
                    is GamePlayViewModel.Status.OnStart -> it.setupUIForStart()
                    is GamePlayViewModel.Status.ToCorrect -> it.onToCorrect()
                    is GamePlayViewModel.Status.ChangedCell -> it.onChangedCell()
                    is GamePlayViewModel.Status.ToError -> it.onToError()
                    is GamePlayViewModel.Status.Completed -> gamePlayViewModel.emitCompleted()
                }
            }
        }


    }

    private val gamePlayViewModel: GamePlayViewModel by lazy { sudokuStageViewModels() }

    private fun onOtherSudokuStart() {
        localGamePlayViewModel.start()
        localGamePlayViewModel.playAuto()
    }

    private val localGamePlayViewModel: GamePlayViewModel by viewModels()

    private fun onOtherSudokuCompleted() {
        localGamePlayViewModel.cancelPlayAuto()
    }


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 게임 준비를 위한 view 노출
     * @param
     * @return
     **/
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

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 에러 셀 해제
     **/
    private fun GamePlayViewModel.Status.ToCorrect.onToCorrect() =
        set.mapNotNull { (it as? IntCoordinateCellEntity)?.run { Pair(row, column) } }
            .forEach { binding.sudokuBoard.setError(it.first, it.second, false) }

    private fun GamePlayViewModel.Status.ChangedCell.onChangedCell() =
        binding.sudokuBoard.setCellValue(row, column, value ?: 0)

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 에러 셀로 변환
     **/
    private fun GamePlayViewModel.Status.ToError.onToError() =
        set.mapNotNull { (it as? IntCoordinateCellEntity)?.run { Pair(row, column) } }
            .forEach { binding.sudokuBoard.setError(it.first, it.second, true) }
}