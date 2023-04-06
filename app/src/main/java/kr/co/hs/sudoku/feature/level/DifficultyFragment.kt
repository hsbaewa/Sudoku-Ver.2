package kr.co.hs.sudoku.feature.level

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.button.MaterialButton
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutSelectStageBinding
import kr.co.hs.sudoku.extension.platform.TextViewExtension.setAutoSizeText
import kr.co.hs.sudoku.feature.level.LevelActivity.Companion.startSelectLevel
import kr.co.hs.sudoku.core.Activity

class DifficultyFragment : Fragment() {

    companion object {
        fun new() = DifficultyFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = with(LayoutSelectStageBinding.inflate(inflater, container, false)) {
        lifecycleOwner = this@DifficultyFragment
        root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DataBindingUtil.getBinding<LayoutSelectStageBinding>(view)?.run {
            btnBeginner.setupUIBeginner()
            btnIntermediate.setupUIIntermediate()
            btnAdvanced.setupUIAdvanced()
        }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 초보자 스테이지 버튼 설정
     **/
    private fun MaterialButton.setupUIBeginner() {
        initSelectLevelButton()
        setOnClickListener { activity.startSelectLevel(Activity.Difficulty.BEGINNER) }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 버튼 공통 설정
     **/
    private fun MaterialButton.initSelectLevelButton() {
        setAutoSizeText()
    }


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 중급자 버튼 설정
     **/
    private fun MaterialButton.setupUIIntermediate() {
        initSelectLevelButton()
        setOnClickListener { activity.startSelectLevel(Activity.Difficulty.INTERMEDIATE) }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 상급자 버튼 설정
     **/
    private fun MaterialButton.setupUIAdvanced() {
        initSelectLevelButton()
        setOnClickListener { activity.startSelectLevel(Activity.Difficulty.ADVANCED) }
    }
}