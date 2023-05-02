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
import kr.co.hs.sudoku.extension.NumberExtension.toTimerFormat
import kr.co.hs.sudoku.extension.platform.ActivityExtension.replaceFragment
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showSnackBar
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.history.impl.HistoryQueueImpl
import kr.co.hs.sudoku.repository.timer.TimerImpl
import kr.co.hs.sudoku.viewmodel.RecordViewModel
import kr.co.hs.sudoku.viewmodel.GamePlayViewModel

class SinglePlayActivity : Activity() {
    companion object {
        fun Activity.startPlayActivity(matrix: IntMatrix?) =
            startActivity(
                Intent(this, SinglePlayActivity::class.java)
                    .putSudokuMatrix(matrix)
            )
    }

    private val gamePlayViewModel: GamePlayViewModel
            by lazy { gamePlayViewModels() }
    private val recordViewModel: RecordViewModel
            by lazy { recordViewModels() }

    lateinit var binding: ActivityPlayBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_play)

        lifecycleScope.launch {
            withStarted {
                initMatrix()
            }

            gamePlayViewModel.statusFlow.collect {
                when (it) {
                    is GamePlayViewModel.Status.OnStart -> it.onStartSudoku()
                    is GamePlayViewModel.Status.ChangedCell -> it.onChangedSudokuCell()
                    is GamePlayViewModel.Status.Completed -> it.onCompletedSudoku()
                    else -> {}
                }
            }
        }

        recordViewModel.timer.observe(this) {
            binding.tvTimer.text = it
        }

        binding.btnRetry.setupUIRetry()
    }

    private fun initMatrix() {
        getSudokuMatrix()
            .takeIf { it != null }
            ?.run {
                replaceFragment(R.id.rootLayout, SudokuPlayFragment.new())
                gamePlayViewModel.buildSudokuMatrix(this)
            }
    }

    private fun GamePlayViewModel.Status.OnStart.onStartSudoku() {
        if (recordViewModel.isRunningCapturedHistoryEvent())
            return

        recordViewModel.bind(stage)
        recordViewModel.setTimer(TimerImpl())
        recordViewModel.setHistoryWriter(HistoryQueueImpl())
        recordViewModel.play()
    }

    private fun GamePlayViewModel.Status.ChangedCell.onChangedSudokuCell() {
        showSnackBar("(${row}, ${column})셀의 값이 $value 로 변경됨")
    }

    private fun GamePlayViewModel.Status.Completed.onCompletedSudoku() {
        with(recordViewModel) {
            if (isRunningCapturedHistoryEvent()) {
                stopCapturedHistory()
            } else {
                stop()
                if (stage.isSudokuClear() && stage.getClearTime() >= 0) {
                    showCompleteRecordDialog(stage.getClearTime())
                }
            }
        }
    }

    private fun showCompleteRecordDialog(clearRecord: Long) {
        val dlgBinding =
            LayoutCompleteBinding.inflate(LayoutInflater.from(this@SinglePlayActivity))
        dlgBinding.tvRecord.text = clearRecord.toTimerFormat()
        dlgBinding.lottieAnim.playAnimation()
        MaterialAlertDialogBuilder(this@SinglePlayActivity)
            .setView(dlgBinding.root)
            .setNegativeButton(R.string.confirm) { _, _ -> finish() }
            .setNeutralButton(R.string.show_replay) { _, _ -> replay() }
            .setPositiveButton(R.string.retry) { _, _ -> retry() }
            .setCancelable(false)
            .show()
    }

    private fun replay() {
        replaceFragment(R.id.rootLayout, SudokuHistoryFragment.new())
        gamePlayViewModel.backToStartingMatrix()
        recordViewModel.playCapturedHistory()
    }

    private fun retry() {
        initMatrix()
        recordViewModel.stop()
        binding.tvTimer.text = 0L.toTimerFormat()
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