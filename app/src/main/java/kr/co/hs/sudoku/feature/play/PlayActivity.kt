package kr.co.hs.sudoku.feature.play

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.ActivityPlayBinding
import kr.co.hs.sudoku.databinding.LayoutCompleteBinding
import kr.co.hs.sudoku.extension.platform.ActivityExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.replaceFragment
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showSnackBar
import kr.co.hs.sudoku.viewmodel.SudokuViewModel
import kr.co.hs.sudoku.viewmodel.TimerViewModel

class PlayActivity : Activity() {
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
                sudokuViewModel.requestMatrix()
            }

            sudokuViewModel.sudokuStatusFlow.collect {
                when (it) {
                    is SudokuViewModel.SudokuStatus.ChangedCell -> {
                        showSnackBar("(${it.row}, ${it.column})셀의 값이 ${it.value} 로 변경됨")
                    }
                    SudokuViewModel.SudokuStatus.Completed -> onCompleteSudoku()
                    is SudokuViewModel.SudokuStatus.OnStart -> onStartSudoku()
                    else -> {}
                }
            }
        }
    }

    private fun onStartSudoku() {
        // 타이머 시작
        timerViewModel.start()
    }

    private fun onCompleteSudoku() {
        timerViewModel.takeIf { it.isRunning() }?.run { stop() }
        val dlgBinding = LayoutCompleteBinding.inflate(LayoutInflater.from(this))
        dlgBinding.tvRecord.text = timerViewModel.time.value
        MaterialAlertDialogBuilder(this)
            .setView(dlgBinding.root)
            .setNegativeButton(R.string.confirm) { _, _ -> finish() }
            .setNeutralButton(R.string.show_replay) { _, _ -> }
            .setPositiveButton(R.string.retry) { _, _ ->
                showProgressIndicator()
                sudokuViewModel.loadStage(getLevel())
            }
            .setCancelable(false)
            .show()
    }
}