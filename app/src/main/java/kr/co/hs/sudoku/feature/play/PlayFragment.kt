package kr.co.hs.sudoku.feature.play

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutPlayGameBinding
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showProgressIndicator
import kr.co.hs.sudoku.model.stage.CellEntity
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.viewmodel.SudokuViewModel
import kr.co.hs.sudoku.views.CountDownView
import kr.co.hs.sudoku.views.SudokuBoardView

class PlayFragment : Fragment() {
    companion object {
        fun new(level: Int) = PlayFragment().apply {
            arguments = Bundle().apply { putLevel(level) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = with(LayoutPlayGameBinding.inflate(inflater, container, false)) {
        binding = this
        lifecycleOwner = this@PlayFragment
        root
    }

    private lateinit var binding: LayoutPlayGameBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            withStarted {
                showProgressIndicator()
                sudokuViewModel.loadStage(getLevel())
            }
            sudokuViewModel.sudokuStatusFlow.collect {
                when (it) {
                    is SudokuViewModel.SudokuStatus.OnReady -> binding.setupUIForReady(it.stage)
                    is SudokuViewModel.SudokuStatus.OnStart -> binding.setupUIForStart(it.stage)
                    is SudokuViewModel.SudokuStatus.ToCorrect -> binding.sudokuBoard.toCorrect(it.set)
                    is SudokuViewModel.SudokuStatus.ToError -> binding.sudokuBoard.toError(it.set)
                    else -> {}
                }
            }
        }
    }

    private val sudokuViewModel: SudokuViewModel by lazy { sudokuViewModels() }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 게임 준비를 위한 view 노출
     * @param
     * @return
     **/
    private fun LayoutPlayGameBinding.setupUIForReady(stage: Stage) {
        sudokuBoard.readySudoku(stage)
        sudokuBoard.clearAllCellValue(stage)

//        viewSilhouette.setupUISilhouette(true)
        viewSilhouette.isVisible = true
        tvCountDown.isVisible = true
        btnReadyComplete.isVisible = true
        btnReadyComplete.setupUIReady(binding.tvCountDown)
//        setupUI(true)
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 게임 준비
     * @param stage
     **/
    private fun SudokuBoardView.readySudoku(stage: Stage) {
        dismissProgressIndicator()
        setupUI(stage)
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment 스도쿠 뷰 setup
     * @param stage 스도쿠 뷰의 데이터 모델
     **/
    private fun SudokuBoardView.setupUI(stage: Stage) {
        setRowCount(stage.rowCount, stage.toValueTable())
        isVisible = true
        cellTouchDownListener = onCellTouchDown()
        cellValueChangedListener = onCellValueChangedListener()
    }


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment SudokuBoardTouch 시점
     * @param
     * @return
     **/
    private fun onCellTouchDown() =
        { row: Int, column: Int -> sudokuViewModel.isMutableCell(row, column) }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment SudokuBoardView의 셀 값이 변경 된 경우 콜백
     **/
    private fun onCellValueChangedListener() =
        { row: Int, column: Int, value: Int? -> sudokuViewModel[row, column] = value ?: 0; true }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 셀 내부의 값들을 초기화 함
     * @param stage
     **/
    private fun SudokuBoardView.clearAllCellValue(stage: Stage) {
        (0 until stage.rowCount).forEach { row ->
            (0 until stage.columnCount).forEach { column ->
                setCellValue(row, column, 0)
            }
        }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 준비 버튼 ui 설정
     * @param with 카운트다운 표시를 위한 view
     **/
    private fun Button.setupUIReady(with: CountDownView) {
//        isVisible = visible
        setOnClickListener {
            isVisible = false
            with.start(3) { sudokuViewModel.start() }
        }
    }

    private fun LayoutPlayGameBinding.setupUIForStart(stage: Stage) {
        sudokuBoard.fillCellValue(stage)

        viewSilhouette.isVisible = false
        btnReadyComplete.isVisible = false
        tvCountDown.isVisible = false
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
     * @param set 대상이 되는 셀 정보
     **/
    private fun SudokuBoardView.toCorrect(set: Set<CellEntity<Int>>) =
        set.mapNotNull { (it as? IntCoordinateCellEntity)?.run { Pair(row, column) } }
            .forEach { setError(it.first, it.second, false) }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 에러 셀로 변환
     * @param set 대상이 되는 셀 정보
     **/
    private fun SudokuBoardView.toError(set: Set<CellEntity<Int>>) =
        set.mapNotNull { (it as? IntCoordinateCellEntity)?.run { Pair(row, column) } }
            .forEach { setError(it.first, it.second, true) }
}