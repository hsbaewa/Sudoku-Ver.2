package kr.co.hs.sudoku.feature

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityGuideBinding
import kr.co.hs.sudoku.views.SudokuView

class GuideActivity : Activity() {

    companion object {
        private fun newIntent(context: Context) = Intent(context, GuideActivity::class.java)
        fun start(context: Context) = context.startActivity(newIntent(context))
    }

    private val binding: ActivityGuideBinding
            by lazy { DataBindingUtil.setContentView(this, R.layout.activity_guide) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        binding.sudokuView.setFixedCellValues(
            listOf(
                listOf(1, 2, 3, 4),
                listOf(3, 0, 1, 2),
                listOf(2, 3, 4, 1),
                listOf(0, 1, 2, 0)
            )
        )
        binding.sudokuView.isVisibleNumber = true

        lifecycleScope.launch {

            // TODO : 아래 코드는 예시
            with(binding.sudokuView) {
                post {
                    isVisibleBoxGuide = false
                    isVisibleRowGuide = false
                    showGuide(1, 1)


                    showGuideForCell(1, 1, "제목", "설명") {
                        lifecycleScope.launch {
                            setTouchDown(1, 1)
                            delay(1000)

                            setTouchSelectNumber(3)
                            delay(1000)

                            showGuideForNumberPad(3, "제목2", "설명2") {}
                            delay(1000)
                        }
                    }
                }


            }

        }
    }

    private fun SudokuView.showGuideForCell(
        row: Int,
        column: Int,
        title: String,
        summary: String,
        onDismissed: () -> Unit
    ) {
        TapTarget.forBounds(getCellBound(row, column), title, summary)
            .apply {
                transparentTarget(true)
                outerCircleColor(R.color.gray_700)
                outerCircleAlpha(0.5f)
            }
            .apply {
                TapTargetView.showFor(this@GuideActivity, this, object : TapTargetView.Listener() {
                    override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                        super.onTargetDismissed(view, userInitiated)
                        onDismissed()
                    }
                })
            }
    }

    private fun SudokuView.showGuideForNumberPad(
        number: Int,
        title: String,
        summary: String,
        onDismissed: () -> Unit
    ) {
        TapTarget.forBounds(getNumberBound(number), title, summary)
            .apply {
                transparentTarget(true)
                outerCircleColor(R.color.gray_700)
                outerCircleAlpha(0.5f)
            }
            .apply {
                TapTargetView.showFor(this@GuideActivity, this, object : TapTargetView.Listener() {
                    override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                        super.onTargetDismissed(view, userInitiated)
                        onDismissed()
                    }
                })
            }
    }


}