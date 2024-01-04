package kr.co.hs.sudoku.feature.challenge.play

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityPlaySingleBinding
import kr.co.hs.sudoku.databinding.LayoutCompleteBinding
import kr.co.hs.sudoku.extension.NumberExtension.toTimerFormat
import kr.co.hs.sudoku.extension.platform.ActivityExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.isShowProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showSnackBar
import kr.co.hs.sudoku.feature.ad.AdaptiveBannerAdManager
import kr.co.hs.sudoku.feature.stage.StageFragment
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.history.impl.CachedHistoryQueue
import kr.co.hs.sudoku.repository.timer.RealServerTimer
import kr.co.hs.sudoku.viewmodel.RecordViewModel
import java.io.File
import java.io.FileOutputStream

class ChallengePlayActivity : Activity(), IntCoordinateCellEntity.ValueChangedListener {
    companion object {
        private const val TAG = "ChallengePlayActivity"
        private fun debug(msg: String) = Log.d(TAG, msg)

        private const val EXTRA_CHALLENGE_ID = "EXTRA_CHALLENGE_ID"

        private fun newIntent(context: Context, challengeId: String) =
            Intent(context, ChallengePlayActivity::class.java)
                .putExtra(EXTRA_CHALLENGE_ID, challengeId)

        fun start(context: Context, challengeId: String) =
            context.startActivity(newIntent(context, challengeId))
    }


    /**
     * 시간 관련
     */
    private val realServerTimer by lazy { RealServerTimer() }

    private fun RealServerTimer.setLastCheckedTime(challengeEntity: ChallengeEntity) {
        val startPlayAt = challengeEntity.startPlayAt
        if (challengeEntity.isPlaying && startPlayAt != null) {
            pass(getCurrentTime() - startPlayAt.time)
        }
    }

    /**
     * 셀 히스토리 관련
     */
    private val historyCacheFile: File
            by lazy { File(cacheDir, "challenge_$challengeId.cache") }
    private val historyQueue: CachedHistoryQueue
            by lazy { CachedHistoryQueue(FileOutputStream(historyCacheFile, true)) }

    private suspend fun getLastSudokuMatrix() = historyCacheFile
        .takeIf { it.exists() && it.length() > 0 }
        ?.run { withContext(Dispatchers.IO) { historyQueue.load(inputStream()) } }

    /**
     * 챌린지 매트릭스
     */
    private val startingMatrix: IntMatrix
        get() = challengePlayViewModel.challengeEntity.value?.matrix
            ?: throw Exception("도전 정보가 없습니다. 다시 시도해 주세요.")

    private val challengeId: String
        get() = intent?.getStringExtra(EXTRA_CHALLENGE_ID)
            ?: throw Exception("도전 정보가 없습니다. 다시 시도해 주세요.")


    private val recordViewModel: RecordViewModel by viewModels()
    private val challengePlayViewModel: ChallengePlayViewModel by viewModels {
        val app = applicationContext as App
        ChallengePlayViewModel.ProviderFactory(
            challengeId,
            app.getChallengeRepository()
        )
    }

    private val binding: ActivityPlaySingleBinding
            by lazy { DataBindingUtil.setContentView(this, R.layout.activity_play_single) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recordViewModel.timer.observe(this) { binding.tvTimer.text = it }


        with(challengePlayViewModel) {
            isRunningProgress.observe(this@ChallengePlayActivity) { isShowProgressIndicator = it }
            challengeEntity.observe(this@ChallengePlayActivity) {
                realServerTimer.setLastCheckedTime(it)
                lifecycleScope.launch {
                    getLastSudokuMatrix()
                        ?.run { sudokuFragment.resume(this) }
                        ?: run { sudokuFragment.init() }
                }

            }
            command.observe(this@ChallengePlayActivity) {
                when (it) {
                    is ChallengePlayViewModel.Started -> {
                        lifecycleScope.launch {
                            val challenge = challengePlayViewModel.challengeEntity.value
                            if (getLastSudokuMatrix() == null && challenge != null) {
                                withContext(Dispatchers.IO) { historyQueue.createHeader(challenge.matrix) }
                            }

                            with(recordViewModel) {
                                sudokuFragment.bindStage(this)
                                setTimer(realServerTimer)
                                setHistoryWriter(historyQueue)
                                play()
                            }
                        }
                    }

                    is ChallengePlayViewModel.Cleared -> {
                        historyCacheFile.delete()
                        showCompleteRecordDialog(it.clearRecord)
                    }

                    else -> {}
                }
            }
        }


        lifecycleScope.launch(CoroutineExceptionHandler { _, t -> showSnackBar(t.message.toString()) }) {
            showProgressIndicator()
            withContext(Dispatchers.IO) {
                realServerTimer.initTime()
                historyQueue
            }
            dismissProgressIndicator()
            withStarted {
                challengePlayViewModel.requestChallenge()
                AdaptiveBannerAdManager(this@ChallengePlayActivity).attachBanner(binding.layoutAdView)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.play_challenge, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_exit, android.R.id.home -> {
                showExitDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onChanged(cell: IntCoordinateCellEntity) {
        if (sudokuFragment.isCleared()) {
            if (recordViewModel.isRunningCapturedHistoryEvent()) {
                recordViewModel.stopCapturedHistory()
            } else {
                recordViewModel.stop()
                val record = sudokuFragment.getClearTime()
                challengePlayViewModel.setRecord(record)
            }
        }
    }


    private val sudokuFragment: ChallengePlayControlStageFragment
            by lazy {
                StageFragment.newInstance<ChallengePlayControlStageFragment>(startingMatrix)
                    .apply { setValueChangedListener(this@ChallengePlayActivity) }
            }

    private fun ChallengePlayControlStageFragment.init() =
        with(supportFragmentManager.beginTransaction()) {
            replace(R.id.layout_control, this@init)
            runOnCommit { challengePlayViewModel.initMatrix() }
            commit()
        }

    private fun ChallengePlayControlStageFragment.resume(lastStatus: List<List<Int>>) =
        with(supportFragmentManager.beginTransaction()) {
            replace(R.id.layout_control, this@resume)
            runOnCommit { challengePlayViewModel.start(lastStatus) }
            commit()
        }

    private fun showCompleteRecordDialog(clearRecord: Long) {
        val dlgBinding =
            LayoutCompleteBinding.inflate(LayoutInflater.from(this))
        dlgBinding.tvRecord.text = clearRecord.toTimerFormat()
        dlgBinding.lottieAnim.playAnimation()
        MaterialAlertDialogBuilder(this)
            .setView(dlgBinding.root)
            .setNegativeButton(R.string.confirm) { _, _ -> finish() }
            .setNeutralButton(R.string.show_replay) { _, _ -> replay() }
            .setCancelable(false)
            .show()
    }

    private fun replay() {
        recordViewModel.playCapturedHistory()
        realServerTimer.pass(0)
        challengePlayViewModel.startReplay()
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.exit_confirm_for_single)
            .setPositiveButton(R.string.confirm) { _, _ -> navigateUpToParent() }
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(false)
            .show()
    }
}