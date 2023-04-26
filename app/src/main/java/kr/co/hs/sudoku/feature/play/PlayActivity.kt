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
import kr.co.hs.sudoku.extension.platform.ActivityExtension.hasFragment
import kr.co.hs.sudoku.extension.platform.ActivityExtension.replaceFragment
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showSnackBar
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.repository.timer.TimerImpl
import kr.co.hs.sudoku.viewmodel.RecordViewModel
import kr.co.hs.sudoku.viewmodel.GamePlayViewModel

class PlayActivity : Activity() {
    companion object {
        fun Activity.startPlayActivity(matrix: IntMatrix?) =
            startActivity(
                Intent(this, PlayActivity::class.java)
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
                    is GamePlayViewModel.Status.ChangedCell -> showSnackBar("(${it.row}, ${it.column})셀의 값이 ${it.value} 로 변경됨")
                    is GamePlayViewModel.Status.Completed -> recordViewModel.stopTimer()
                    is GamePlayViewModel.Status.OnStart -> onStartSudoku(it.stage)
                    else -> {}
                }
            }
        }

        recordViewModel.timer.observe(this) {
            setTime(it)
            if (hasFragment(PlayFragment::class.java) && gamePlayViewModel.isCompleted()) {
                showCompleteRecordDialog(it)
            }
        }

        binding.btnRetry.setupUIRetry()
    }

    private fun initMatrix() {
        getSudokuMatrix()
            .takeIf { it != null }
            ?.run {
                replaceFragment(R.id.rootLayout, PlayFragment.new())
                gamePlayViewModel.buildSudokuMatrix(this)
            }
    }

    private fun setTime(formattedTime: String) {
        binding.tvTimer.text = formattedTime
    }

    private fun onStartSudoku(stage: Stage) {
        recordViewModel.setTimer(TimerImpl())
        recordViewModel.startTimer()
        recordViewModel.initCaptureTarget(stage)
    }

    private fun showCompleteRecordDialog(clearRecord: String) {
        val dlgBinding =
            LayoutCompleteBinding.inflate(LayoutInflater.from(this@PlayActivity))
        dlgBinding.tvRecord.text = clearRecord
        MaterialAlertDialogBuilder(this@PlayActivity)
            .setView(dlgBinding.root)
            .setNegativeButton(R.string.confirm) { _, _ -> finish() }
            .setNeutralButton(R.string.show_replay) { _, _ -> replay() }
            .setPositiveButton(R.string.retry) { _, _ -> retry() }
            .setCancelable(false)
            .show()
    }

    private fun replay() {
        replaceFragment(R.id.rootLayout, ReplayFragment.new())
        gamePlayViewModel.backToStartingMatrix()
        recordViewModel.startTimer()
    }

    private fun retry() {
        initMatrix()
        recordViewModel.stopTimer()
        recordViewModel.clearTime()
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