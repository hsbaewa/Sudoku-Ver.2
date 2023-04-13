package kr.co.hs.sudoku.feature.play

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
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
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.viewmodel.TimerLogViewModel
import kr.co.hs.sudoku.viewmodel.SudokuStatusViewModel
import kr.co.hs.sudoku.viewmodel.SudokuStageViewModel

class PlayActivity : Activity() {
    companion object {
        fun Activity.startPlayActivity(difficulty: Difficulty, level: Int) =
            startActivity(
                Intent(this, PlayActivity::class.java)
                    .putDifficulty(difficulty)
                    .putLevel(level)
            )
    }

    private val sudokuStageViewModel: SudokuStageViewModel
            by lazy { sudokuStageViewModels(getDifficulty()) }
    private val timerLogViewModel: TimerLogViewModel
            by lazy { timerLogViewModels() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding =
            DataBindingUtil.setContentView<ActivityPlayBinding>(this, R.layout.activity_play)

        sudokuStageViewModel.matrixList.observe(this) {
            dismissProgressIndicator()
            replaceFragment(R.id.rootLayout, PlayFragment.new(getLevel()))
        }

        timerLogViewModel.time.observe(this) {
            binding.tvTimer.text = it
        }

        lifecycleScope.launch {
            withStarted {
                showProgressIndicator()
                sudokuStageViewModel.requestMatrix()
            }

            sudokuStageViewModel.statusFlow.collect {
                when (it) {
                    is SudokuStatusViewModel.Status.ChangedCell -> {
                        showSnackBar("(${it.row}, ${it.column})셀의 값이 ${it.value} 로 변경됨")
                    }
                    SudokuStatusViewModel.Status.Completed -> onCompleteSudoku()
                    is SudokuStatusViewModel.Status.OnStart -> onStartSudoku(it.stage)
                    else -> {}
                }
            }
        }

        binding.btnRetry.setupUIRetry()
    }

    private fun onStartSudoku(stage: Stage) {
        // 타이머 시작
        timerLogViewModel.startWithRecord(stage)
    }

    private fun onCompleteSudoku() {
        timerLogViewModel.takeIf { it.isRunning() }?.run { stop() }
        val dlgBinding = LayoutCompleteBinding.inflate(LayoutInflater.from(this))
        dlgBinding.tvRecord.text = timerLogViewModel.time.value
        MaterialAlertDialogBuilder(this)
            .setView(dlgBinding.root)
            .setNegativeButton(R.string.confirm) { _, _ -> finish() }
            .setNeutralButton(R.string.show_replay) { _, _ -> replay() }
            .setPositiveButton(R.string.retry) { _, _ -> retry() }
            .setCancelable(false)
            .show()
    }

    private fun replay() {
        replaceFragment(R.id.rootLayout, ReplayFragment.new())
        timerLogViewModel.start()
    }

    private fun retry() {
        showProgressIndicator()
        replaceFragment(R.id.rootLayout, PlayFragment.new(getLevel()))
        timerLogViewModel.stop()
        sudokuStageViewModel.loadStage(getLevel())
    }

    private fun ImageButton.setupUIRetry() {
        setOnClickListener { showRetryPopup() }
    }

    private fun showRetryPopup() =
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.retry_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.confirm) { _, _ -> retry() }
            .setCancelable(false)
            .show()
}