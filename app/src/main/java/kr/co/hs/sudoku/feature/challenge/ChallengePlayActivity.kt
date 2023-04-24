package kr.co.hs.sudoku.feature.challenge

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityPlayChallengeBinding
import kr.co.hs.sudoku.databinding.LayoutCompleteBinding
import kr.co.hs.sudoku.extension.platform.ActivityExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.hasFragment
import kr.co.hs.sudoku.extension.platform.ActivityExtension.replaceFragment
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showSnackBar
import kr.co.hs.sudoku.feature.play.PlayFragment
import kr.co.hs.sudoku.feature.play.ReplayFragment
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.repository.ProfileRepositoryImpl
import kr.co.hs.sudoku.repository.challenge.ChallengeReaderRepositoryImpl
import kr.co.hs.sudoku.repository.record.ChallengeRecordRepository
import kr.co.hs.sudoku.usecase.record.PutRecordUseCaseImpl
import kr.co.hs.sudoku.usecase.user.GetProfileUseCase
import kr.co.hs.sudoku.viewmodel.ChallengeViewModel
import kr.co.hs.sudoku.viewmodel.GamePlayViewModel
import kr.co.hs.sudoku.viewmodel.RecordViewModel

class ChallengePlayActivity : Activity() {
    companion object {
        fun Activity.startChallengePlayActivity(challengeId: String, uid: String) =
            startActivity(
                Intent(this, ChallengePlayActivity::class.java)
                    .putChallengeId(challengeId)
                    .putUserId(uid)
            )

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_play_challenge)
        uid = getUserId() ?: throw IllegalArgumentException("unknown user")
        challengeId = getExtraForChallengeId() ?: throw IllegalArgumentException("unknown challenge")

        replaceFragment(R.id.rootLayout, PlayFragment.new())

        challengeViewModel.let {
            it.isRunningProgress.observe(this) { onChangedRunningProgressState(it) }
            it.error.observe(this) { onError(it) }
            it.challenge.observe(this) { gamePlayViewModel.setSudokuMatrix(it.matrix) }
            it.requestChallenge(ChallengeReaderRepositoryImpl(), challengeId)
        }

        recordViewModel.timer.observe(this) {
            binding.tvTimer.text = it
            if (hasFragment(PlayFragment::class.java) && gamePlayViewModel.isCompleted()) {
                showCompleteRecordDialog(it)
            }
        }

        lifecycleScope.launch {
            gamePlayViewModel.statusFlow.collect {
                when (it) {
                    is GamePlayViewModel.Status.Completed -> recordViewModel.stopTimer()
                    is GamePlayViewModel.Status.OnStart -> onStartSudoku(it.stage)
                    else -> {}
                }
            }
        }

    }

    lateinit var binding: ActivityPlayChallengeBinding
    lateinit var uid: String
    lateinit var challengeId: String
    private val challengeViewModel: ChallengeViewModel by lazy { challengeRankingViewModels() }

    private fun onChangedRunningProgressState(progress: Boolean) = if (progress) {
        showProgressIndicator()
    } else {
        dismissProgressIndicator()
    }

    private fun onError(t: Throwable) = showSnackBar(t.message.toString())

    private val gamePlayViewModel: GamePlayViewModel by lazy { gamePlayViewModels() }

    private val recordViewModel: RecordViewModel by lazy { recordViewModels() }


    private fun showCompleteRecordDialog(clearRecord: String) {
        lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            dismissProgressIndicator()
            showSnackBar(throwable.message.toString())
        }) {
            showProgressIndicator()
            val profile = withContext(Dispatchers.IO) { getProfile() }
            val record = RankerEntity(profile, recordViewModel.lastHistoryTime())
            val recordRepository = ChallengeRecordRepository(challengeId)
            withContext(Dispatchers.IO) {
                PutRecordUseCaseImpl(recordRepository).invoke(record).last()
            }
            dismissProgressIndicator()

            val dlgBinding =
                LayoutCompleteBinding.inflate(LayoutInflater.from(this@ChallengePlayActivity))
            dlgBinding.tvRecord.text = clearRecord
            MaterialAlertDialogBuilder(this@ChallengePlayActivity)
                .setView(dlgBinding.root)
                .setNegativeButton(R.string.confirm) { _, _ -> finish() }
                .setNeutralButton(R.string.show_replay) { _, _ -> replay() }
                .setCancelable(false)
                .show()
        }
    }

    private suspend fun getProfile() =
        GetProfileUseCase(ProfileRepositoryImpl()).invoke(uid).last()

    private fun replay() {
        replaceFragment(R.id.rootLayout, ReplayFragment.new())
        gamePlayViewModel.backToStartingMatrix()
        recordViewModel.startTimer()
    }

    private fun onStartSudoku(stage: Stage) {
        recordViewModel.startTimer()
        recordViewModel.initCaptureTarget(stage)
    }
}