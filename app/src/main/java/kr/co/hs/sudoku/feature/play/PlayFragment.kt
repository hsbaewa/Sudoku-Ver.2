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
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dataStore
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showProgressIndicator
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.repository.GameSettingsRepositoryImpl
import kr.co.hs.sudoku.viewmodel.SudokuStatusViewModel
import kr.co.hs.sudoku.viewmodel.SudokuStageViewModel
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
            }
            sudokuStageViewModel.statusFlow.collect {
                when (it) {
                    is SudokuStatusViewModel.Status.OnReady -> it.setupUIForReady()
                    is SudokuStatusViewModel.Status.OnStart -> it.setupUIForStart()
                    is SudokuStatusViewModel.Status.ToCorrect -> it.onToCorrect()
                    is SudokuStatusViewModel.Status.ToError -> it.onToError()
                    else -> {}
                }
            }
        }
    }

    private val sudokuStageViewModel: SudokuStageViewModel by lazy { sudokuStageViewModels() }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment 게임 준비를 위한 view 노출
     * @param
     * @return
     **/
    private fun SudokuStatusViewModel.Status.OnReady.setupUIForReady() {
        binding.sudokuBoard.run {
            readySudoku(stage)
            clearAllCellValue(stage)
            gameSettingsViewModel.gameSettings.observe(viewLifecycleOwner) {
                enabledHapticFeedback = it.enabledHapticFeedback
            }
        }
        binding.viewSilhouette.isVisible = true
        binding.tvCountDown.isVisible = true
        binding.btnReadyComplete.run {
            isVisible = true
            setupUIReady(binding.tvCountDown)
        }
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
        { row: Int, column: Int -> sudokuStageViewModel.isMutableCell(row, column) }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment SudokuBoardView 의 셀 값이 변경 된 경우 콜백
     **/
    private fun onCellValueChangedListener() =
        { row: Int, column: Int, value: Int? ->
            sudokuStageViewModel[row, column] = value ?: 0; true
        }

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
     * @param with 카운트 다운 표시를 위한 view
     **/
    private fun Button.setupUIReady(with: CountDownView) {
        setOnClickListener {
            isVisible = false
            with.start(3) { sudokuStageViewModel.start() }
        }
    }

    private val gameSettingsViewModel
            by lazy { gameSettingsViewModels(GameSettingsRepositoryImpl(dataStore)) }

    private fun SudokuStatusViewModel.Status.OnStart.setupUIForStart() {
        binding.sudokuBoard.fillCellValue(stage)
        binding.viewSilhouette.isVisible = false
        binding.btnReadyComplete.isVisible = false
        binding.tvCountDown.isVisible = false
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
}