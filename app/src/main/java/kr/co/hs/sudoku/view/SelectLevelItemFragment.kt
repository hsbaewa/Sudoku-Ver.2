package kr.co.hs.sudoku.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutLevelInfoBinding
import kr.co.hs.sudoku.model.stage.StageBuilder
import kr.co.hs.sudoku.viewmodel.StageListViewModel

class SelectLevelItemFragment : Fragment() {
    companion object {
        fun newInstance(level: Int) = SelectLevelItemFragment().apply {
            arguments = Bundle().apply { putInt(EXTRA_LEVEL, level) }
        }

        private const val EXTRA_LEVEL = "kr.co.hs.sudoku.LevelInfoFragment.EXTRA_LEVEL"
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
            viewLifecycleOwner.lifecycleScope.launch {
                withStarted {
                    sudokuBoard.setupUI(getStage())
                    tvTitle.setupUI(getStageLevel())
                    btnStart.setupUIStart()
                }
            }
        }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment 스도쿠 스테이지 ui setup
     * @param stage 선택된
     **/
    private fun SudokuBoardView.setupUI(stage: StageBuilder?) = stage?.let {
        setRowCount(it.getRowCount())
        (0 until it.getRowCount()).forEach { row ->
            (0 until it.getColumnCount()).forEach { column ->
                post { setEnabled(row, column, !it.isImmutableCell(row, column)) }
            }
        }
        isVisible = true
    } ?: kotlin.run { isVisible = false }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment 선택된 스테이지 리턴
     * @return 선택된 스테이지 리턴
     **/
    private fun getStage() = with(getLevelInfoViewModel()) {
        stageList.value?.get(getStageLevel())
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment ViewModel Getter
     * @return LevelInfoViewModel
     **/
    private fun getLevelInfoViewModel(): StageListViewModel {
        val viewModel: StageListViewModel by activityViewModels()
        return viewModel
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment 레벨 리턴
     * @return level integer
     **/
    private fun getStageLevel() = arguments?.getInt(EXTRA_LEVEL) ?: 0


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment 스테이지 라벨
     * @param level 스테이지 레벨 integer
     **/
    private fun TextView.setupUI(level: Int) {
        text = getString(R.string.level_format, level)
        setAutoSize()
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 텍스트를 뷰 사이즈에 맞게 자동 설정
     **/
    private fun TextView.setAutoSize() =
        TextViewCompat.setAutoSizeTextTypeWithDefaults(
            this,
            TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
        )


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 시작 버튼 설정
     **/
    private fun Button.setupUIStart() {
        setAutoSize()
        setOnClickListener { context.startLevel() }
    }

    private fun Context.startLevel() {
        //TODO 레벨 시작
    }
}