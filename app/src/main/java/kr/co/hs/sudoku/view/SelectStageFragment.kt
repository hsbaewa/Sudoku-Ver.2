package kr.co.hs.sudoku.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.core.widget.TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import kr.co.hs.sudoku.view.SelectLevelActivity.Companion.DIFFICULTY_ADVANCED
import kr.co.hs.sudoku.view.SelectLevelActivity.Companion.DIFFICULTY_BEGINNER
import kr.co.hs.sudoku.view.SelectLevelActivity.Companion.DIFFICULTY_INTERMEDIATE
import kr.co.hs.sudoku.view.SelectLevelActivity.Companion.EXTRA_DIFFICULTY
import kr.co.hs.sudoku.databinding.LayoutSelectStageBinding

class SelectStageFragment : Fragment() {

    companion object {
        fun newInstance() = SelectStageFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = with(LayoutSelectStageBinding.inflate(inflater, container, false)) {
        lifecycleOwner = this@SelectStageFragment
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
        setOnClickListener { context.startSelectLevel(Difficulty.Beginner) }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 버튼 공통 설정
     **/
    private fun MaterialButton.initSelectLevelButton() {
        setAutoSize()
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 텍스트를 뷰 사이즈에 맞게 자동 설정
     **/
    private fun TextView.setAutoSize() =
        TextViewCompat.setAutoSizeTextTypeWithDefaults(this, AUTO_SIZE_TEXT_TYPE_UNIFORM)


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 레벨 선택 화면으로 이동
     * @param difficulty 난이도
     **/
    private fun Context.startSelectLevel(difficulty: Difficulty) =
        with(Intent(this, SelectLevelActivity::class.java)) {
            putExtra(
                EXTRA_DIFFICULTY, when (difficulty) {
                    Difficulty.Beginner -> DIFFICULTY_BEGINNER
                    Difficulty.Intermediate -> DIFFICULTY_INTERMEDIATE
                    Difficulty.Advanced -> DIFFICULTY_ADVANCED
                }
            )
            startActivity(this)
        }

    enum class Difficulty { Beginner, Intermediate, Advanced }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 중급자 버튼 설정
     **/
    private fun MaterialButton.setupUIIntermediate() {
        initSelectLevelButton()
        setOnClickListener { context.startSelectLevel(Difficulty.Intermediate) }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 상급자 버튼 설정
     **/
    private fun MaterialButton.setupUIAdvanced() {
        initSelectLevelButton()
        setOnClickListener { context.startSelectLevel(Difficulty.Advanced) }
    }
}