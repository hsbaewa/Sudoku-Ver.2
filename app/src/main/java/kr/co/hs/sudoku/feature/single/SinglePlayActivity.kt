package kr.co.hs.sudoku.feature.single

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityPlaySingleBinding
import kr.co.hs.sudoku.databinding.LayoutCompleteBinding
import kr.co.hs.sudoku.extension.NumberExtension.toTimerFormat
import kr.co.hs.sudoku.extension.platform.ActivityExtension.isShowProgressIndicator
import kr.co.hs.sudoku.feature.stage.StageFragment
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.matrix.EmptyMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.history.HistoryItem
import kr.co.hs.sudoku.parcel.MatrixParcelModel
import kr.co.hs.sudoku.viewmodel.RecordViewModel

class SinglePlayActivity : Activity(), IntCoordinateCellEntity.ValueChangedListener {
    companion object {
        private const val EXTRA_MATRIX = "EXTRA_MATRIX"

        private fun newIntent(context: Context, matrix: IntMatrix) =
            Intent(context, SinglePlayActivity::class.java)
                .putExtra(EXTRA_MATRIX, MatrixParcelModel(matrix))

        fun start(context: Context, matrix: IntMatrix) =
            context.startActivity(newIntent(context, matrix))
    }

    private val singlePlayViewModel: SinglePlayViewModel
            by viewModels { SinglePlayViewModel.ProviderFactory(startingMatrix) }
    private val recordViewModel: RecordViewModel by viewModels()
    private val binding: ActivityPlaySingleBinding
            by lazy { DataBindingUtil.setContentView(this, R.layout.activity_play_single) }

    private val startingMatrix: IntMatrix by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_MATRIX, MatrixParcelModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_MATRIX)
        }?.matrix
            ?.run { CustomMatrix(this) }
            ?: EmptyMatrix()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        onBackPressedDispatcher.addCallback { showExitDialog() }

        controlBoard {
            it.setStatus(false, getString(R.string.start))
        }

        recordViewModel.timer.observe(this) {
            binding.tvTimer.text = it
        }
        lifecycleScope.launch {
            recordViewModel.cellEventHistoryFlow.collect { item ->
                when (item) {
                    is HistoryItem.Removed ->
                        controlBoard { it.setValue(item.row, item.column, 0) }

                    is HistoryItem.Set ->
                        controlBoard { it.setValue(item.row, item.column, item.value) }
                }
            }
        }
        singlePlayViewModel.isRunningProgress.observe(this) { isShowProgressIndicator = it }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.play_single, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_retry -> {
                showRetryPopup()
                true
            }

            R.id.menu_exit, android.R.id.home -> {
                showExitDialog()
                true
            }

            else -> false
        }
    }

    fun controlBoard(on: (SinglePlayControlStageFragment) -> Unit) = with(supportFragmentManager) {
        val tag = SinglePlayControlStageFragment::class.java.simpleName
        findFragmentByTag(tag)?.run { this as? SinglePlayControlStageFragment }
            ?.also(on)
            ?: with(beginTransaction()) {
                val fragment =
                    StageFragment.newInstance<SinglePlayControlStageFragment>(startingMatrix)
                fragment.setValueChangedListener(this@SinglePlayActivity)
                replace(R.id.layout_control, fragment, tag).runOnCommit { on(fragment) }
            }.commit()
    }

    override fun onChanged(cell: IntCoordinateCellEntity) {
        controlBoard {
            if (it.isCleared()) {

                if (recordViewModel.isRunningCapturedHistoryEvent()) {
                    recordViewModel.stopCapturedHistory()
                } else {
                    stopTimer()
                    val record = it.getClearTime()
                    showCompleteRecordDialog(record)
                }
            }
        }
    }

    fun stopTimer() {
        recordViewModel.stop()
    }

    private fun showCompleteRecordDialog(clearRecord: Long) {
        val dlgBinding =
            LayoutCompleteBinding.inflate(LayoutInflater.from(this@SinglePlayActivity))
        dlgBinding.tvRecord.text = clearRecord.toTimerFormat()
        dlgBinding.lottieAnim.playAnimation()
        MaterialAlertDialogBuilder(this@SinglePlayActivity)
            .setView(dlgBinding.root)
            .setNegativeButton(R.string.confirm) { _, _ -> finish() }
            .setNeutralButton(R.string.show_replay) { _, _ ->
                replay()
            }
            .setPositiveButton(R.string.retry) { _, _ ->
                retry()
            }
            .setCancelable(false)
            .show()
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.exit_confirm_for_single)
            .setPositiveButton(R.string.confirm) { _, _ -> navigateUpToParent() }
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(false)
            .show()
    }

    private fun showRetryPopup() =
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.retry_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.confirm) { _, _ -> retry() }
            .setCancelable(false)
            .show()

    private fun retry() {
        controlBoard {
            it.clearBoard()
            it.setStatus(false, getString(R.string.start))
        }
        recordViewModel.stop()
        binding.tvTimer.text = 0L.toTimerFormat()
    }

    private fun replay() {
        controlBoard {
            it.clearAllStageValues()
        }
        recordViewModel.playCapturedHistory()
    }
}