package kr.co.hs.sudoku.feature.play

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutPlayGameBinding
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showProgressIndicator
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.views.SudokuBoardView
import kotlin.collections.HashSet

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
        lifecycleOwner = this@PlayFragment
        root
    }

    private fun View.getBinding() = DataBindingUtil.getBinding<LayoutPlayGameBinding>(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sudokuViewModels().sudoku.observe(viewLifecycleOwner) {
            dismissProgressIndicator()
            view.getBinding()?.sudokuBoard?.setupUI(it)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            withStarted {
                showProgressIndicator()
                sudokuViewModels().loadStage(getLevel())
            }
        }
    }


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment 스도쿠 뷰 setup
     * @param stage 스도쿠 뷰의 데이터 모델
     **/
    private fun SudokuBoardView.setupUI(stage: Stage) {
        setRowCount(stage.rowCount, stage.toValueTable())
        (0 until stage.rowCount).forEach { row ->
            (0 until stage.columnCount).forEach { column ->
                runCatching { stage[row, column] }.getOrNull()
                    ?.takeIf { it > 0 }
                    ?.run { setCellValue(row, column, this) }
            }
        }
        isVisible = true
        val errorCell = HashSet<Pair<Int, Int>>()
        cellTouchDownListener = onCellTouchDown(stage)
        cellValueChangedListener = onCellValueChangedListener(stage, errorCell)
    }


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment SudokuBoardTouch 시점
     * @param
     * @return
     **/
    private fun onCellTouchDown(stage: Stage) = { row: Int, column: Int ->
        !stage.getCell(row, column).isImmutable()
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment SudokuBoardView의 셀 값이 변경 된 경우 콜백
     * @param stage 대상 Stage
     * @param currentErrorCell 에러 셀 정보
     **/
    private fun SudokuBoardView.onCellValueChangedListener(
        stage: Stage,
        currentErrorCell: HashSet<Pair<Int, Int>>
    ) = { row: Int, column: Int, value: Int? ->
        stage[row, column] = value ?: 0

        currentErrorCell.forEach { setError(it.first, it.second, false) }
        currentErrorCell.clear()

        stage.getDuplicatedCells().toList().forEach {
            val cell = (it as IntCoordinateCellEntity)
            val x = cell.coordinate.x
            val y = cell.coordinate.y
            currentErrorCell.add(x to y)
            setError(x, y, true)
        }

        if (stage.isCompleted()) {
            onCompleted()
        }
        true
    }

    private fun onCompleted() {
        MaterialAlertDialogBuilder(activity)
            .setTitle("완료")
            .setMessage("완료")
            .setPositiveButton("확인") { _, _ ->
                activity.finish()
            }
            .setCancelable(false)
            .show()
    }
}