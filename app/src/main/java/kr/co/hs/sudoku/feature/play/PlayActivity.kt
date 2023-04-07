package kr.co.hs.sudoku.feature.play

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.ActivityPlayBinding
import kr.co.hs.sudoku.extension.platform.ActivityExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.replaceFragment
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showSnackBar
import kr.co.hs.sudoku.viewmodel.SudokuViewModel
import kr.co.hs.sudoku.viewmodel.TimerViewModel

class PlayActivity : Activity(), PlayPresenter {
    companion object {
        fun Activity.startPlayActivity(difficulty: Difficulty, level: Int) =
            startActivity(
                Intent(this, PlayActivity::class.java)
                    .putDifficulty(difficulty)
                    .putLevel(level)
            )
    }

    private val sudokuViewModel: SudokuViewModel
            by lazy { sudokuViewModels(getDifficulty()) }
    private val timerViewModel: TimerViewModel by lazy { timerViewModels() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding =
            DataBindingUtil.setContentView<ActivityPlayBinding>(this, R.layout.activity_play)

        sudokuViewModel.matrixList.observe(this) {
            dismissProgressIndicator()
            replaceFragment(R.id.rootLayout, PlayFragment.new(getLevel()))
        }

        timerViewModel.time.observe(this) {
            binding.tvTimer.text = it
        }

        lifecycleScope.launch {
            withStarted {
                showProgressIndicator()
                sudokuViewModel.requestStageList()
            }
        }
    }

    override fun onStartSudoku() {
        // 타이머 시작
        timerViewModel.start()
    }

    override fun onCompleteSudoku() {
        timerViewModel.takeIf { it.isRunning() }?.run { stop() }
        MaterialAlertDialogBuilder(this)
            .setTitle("완료")
            .setMessage("기록 : ${timerViewModel.time.value}")
            .setPositiveButton("확인") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    override fun onChangedSudokuCell(row: Int, column: Int, value: Int) {
        // FIXME: 차후 제거
        showSnackBar("($row, $column)셀의 값이 $value 로 변경됨")
    }
}