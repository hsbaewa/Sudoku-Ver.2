package kr.co.hs.sudoku.feature.level

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutLevelInfoBinding
import kr.co.hs.sudoku.extension.platform.TextViewExtension.setAutoSizeText
import kr.co.hs.sudoku.feature.play.WithCPUPlayActivity.Companion.startBattlePlayActivity
import kr.co.hs.sudoku.feature.play.SinglePlayActivity.Companion.startPlayActivity
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.views.SudokuBoardView

class LevelFragment : Fragment() {
    companion object {
        fun new(level: Int) = LevelFragment().apply {
            arguments = Bundle().apply { putLevel(level) }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = LayoutLevelInfoBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DataBindingUtil.getBinding<LayoutLevelInfoBinding>(view)?.run {
            tvTitle.setupUI(getLevel())
            btnStart.setupUIStart(radioGroupSelectMode)

            viewLifecycleOwner.lifecycleScope.launch {
                withStarted {
                    launch {
                        sudokuBoard.setupUI(getSudokuMatrix())
                    }
                }
            }
        }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment 스테이지 라벨
     * @param level 스테이지 레벨 integer
     **/
    private fun TextView.setupUI(level: Int) {
        text = getString(R.string.level_format, level)
        setAutoSizeText()
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 시작 버튼 설정
     **/
    private fun Button.setupUIStart(radioGroup: RadioGroup) {
        setAutoSizeText()
        setOnClickListener {
            when (radioGroup.checkedRadioButtonId) {
                R.id.radioBtnSinglePlay ->
                    activity.startPlayActivity(getSudokuMatrix())

                R.id.radioBtnWithCPU ->
                    activity.startBattlePlayActivity(getSudokuMatrix())
            }
        }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment 스도쿠 스테이지 ui setup
     * @param stage 선택된
     **/
    private fun SudokuBoardView.setupUI(stage: IntMatrix?) = stage?.let {
        isVisible = true
        setRowCount(it.rowCount, stage)
    } ?: kotlin.run { isVisible = false }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment 선택된 스테이지 리턴
     * @return 선택된 스테이지 리턴
     **/
    private fun getSudokuMatrix() = with(singlePlayDifficultyViewModels()) {
        matrixList.value?.get(getLevel())
    }

}