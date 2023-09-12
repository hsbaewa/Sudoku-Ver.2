package kr.co.hs.sudoku.feature.challenge

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
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
import kr.co.hs.sudoku.extension.NumberExtension.toTimerFormat
import kr.co.hs.sudoku.extension.platform.ActivityExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.replaceFragment
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showSnackBar
import kr.co.hs.sudoku.feature.play.SudokuPlayFragment
import kr.co.hs.sudoku.feature.play.SudokuHistoryFragment
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.model.stage.history.impl.CachedHistoryQueue
import kr.co.hs.sudoku.model.stage.history.HistoryQueue
import kr.co.hs.sudoku.repository.challenge.ChallengeRepositoryImpl
import kr.co.hs.sudoku.repository.timer.RealServerTimer
import kr.co.hs.sudoku.usecase.record.PutRecordUseCaseImpl
import kr.co.hs.sudoku.viewmodel.ChallengeViewModel
import kr.co.hs.sudoku.viewmodel.GamePlayViewModel
import kr.co.hs.sudoku.viewmodel.RecordViewModel
import java.io.File
import java.io.FileOutputStream

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

        lifecycleScope.launch {
            // 실제 서버 시간 알아오기
            withContext(Dispatchers.IO) { realServerTimer.initTime() }

            // Challenge 정보 가져오기
            requestChallengeInfo()
        }
        // Challenge 정보 응답 모니터
        challengeViewModel.challenge.observe(this, responseChallengeInfo)
        // 에러 이벤트 핸들 설정
        challengeViewModel.error.observe(this) { onError(it) }
        // 프로그레스 이벤트 설정
        challengeViewModel.isRunningProgress.observe(this) { onChangedRunningProgressState(it) }
        // 타이머 시간 이벤트
        recordViewModel.timer.observe(this, timerTickEvent)
    }

    private lateinit var binding: ActivityPlayChallengeBinding

    // 타이머
    private val realServerTimer by lazy { RealServerTimer() }

    private fun requestChallengeInfo() {
        getExtraForChallengeId()?.run {
            challengeViewModel.requestChallenge(app.getChallengeRepository(), this)
        } ?: kotlin.run {
            // TODO ?? challenge id 없으면 어떻게?...
        }
    }

    private val challengeViewModel: ChallengeViewModel by lazy { challengeRankingViewModels() }
    private val responseChallengeInfo = Observer<ChallengeEntity> { challenge ->
        // Challenge를 통해 얻어온 matrix로 game board 설정
        gamePlayViewModel.setSudokuMatrix(challenge.matrix)

        // 내가 진행중이었던 게임 있으면 그 마지막 시간부터 시간 타이머 계산
        realServerTimer.continueIfLastPlaying(challenge)

        // 게임 Fragment 설정
        replaceFragment(R.id.rootLayout, SudokuPlayFragment.new())

        lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            dismissProgressIndicator()
            showSnackBar(throwable.message.toString())
        }) {
            showProgressIndicator()

            val cachedFile = getCachedFile(challenge.challengeId)
            val queue = CachedHistoryQueue(FileOutputStream(cachedFile, true))

            if (cachedFile.length() > 0) {
                val currentStatus = withContext(Dispatchers.IO) {
                    queue.load(cachedFile.inputStream())
                }
                gamePlayViewModel.batch(currentStatus)
            } else {
                withContext(Dispatchers.IO) { queue.createHeader(gamePlayViewModel.getStage()) }
            }

            // 게임 상태 이벤트 수신 설정
            collectGamePlayStatus(challenge, queue)

            dismissProgressIndicator()
        }

    }

    private fun getCachedFile(challengeId: String) = File(cacheDir, "challenge_$challengeId.cache")

    private fun collectGamePlayStatus(challenge: ChallengeEntity, historyQueue: HistoryQueue) =
        lifecycleScope.launch {
            gamePlayViewModel.statusFlow.collect { status ->
                when (status) {
                    is GamePlayViewModel.Status.OnStart -> {
                        lifecycleScope.launch(Dispatchers.IO) { challenge.checkPlaying() }
                        status.onStartSudoku(historyQueue)
                    }

                    is GamePlayViewModel.Status.Completed -> status.onCompetedSudoku()
                    else -> {}
                }
            }
        }

    private fun GamePlayViewModel.Status.OnStart.onStartSudoku(historyQueue: HistoryQueue) {
        if (recordViewModel.isRunningCapturedHistoryEvent())
            return

        recordViewModel.bind(stage)
        recordViewModel.setTimer(realServerTimer)
        recordViewModel.setHistoryWriter(historyQueue)
        recordViewModel.play()
    }

    private fun GamePlayViewModel.Status.Completed.onCompetedSudoku() {
        with(recordViewModel) {
            if (isRunningCapturedHistoryEvent()) {
                stopCapturedHistory()
            } else {
                stop()
                val clearTime = stage.getClearTime()
                if (clearTime >= 0) {
                    val uid = getUserId()
                    val challengeId = challengeViewModel.challenge.value?.challengeId
                    if (uid != null && challengeId != null) {
                        onCompleteSudoku(uid, challengeId, clearTime)
                    }
                }
            }
        }
    }

    private val recordViewModel: RecordViewModel by lazy { recordViewModels() }

    private val timerTickEvent = Observer<String> {
        binding.tvTimer.text = it
    }

    private fun onCompleteSudoku(
        uid: String,
        challengeId: String,
        clearTime: Long
    ) =
        lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            dismissProgressIndicator()
            showSnackBar(throwable.message.toString())
        }) {
            showProgressIndicator()
            withContext(Dispatchers.IO) {
                setClearRecordToServer(uid, challengeId, clearTime)
                getCachedFile(challengeId).delete()
            }
            dismissProgressIndicator()
            showCompleteRecordDialog(clearTime.toTimerFormat())
        }

    private val gamePlayViewModel: GamePlayViewModel by lazy { gamePlayViewModels() }
    private fun RealServerTimer.continueIfLastPlaying(challengeEntity: ChallengeEntity) {
        val startDate = challengeEntity.startPlayAt
        if (challengeEntity.isPlaying && startDate != null) {
            pass(getCurrentTime() - startDate.time)
        }
    }

    private suspend fun ChallengeEntity.checkPlaying() {
        if (!isPlaying) {
            app.getChallengeRepository().setPlaying(challengeId)
        }
    }

    private fun onChangedRunningProgressState(progress: Boolean) = if (progress) {
        showProgressIndicator()
    } else {
        dismissProgressIndicator()
    }

    private fun onError(t: Throwable) = showSnackBar(t.message.toString())

    private suspend fun setClearRecordToServer(uid: String, challengeId: String, clearTime: Long) {
        val profile = getProfile(uid)
        val record = RankerEntity(profile, clearTime)

        val repo = app.getChallengeRepository() as ChallengeRepositoryImpl
        repo.setChallengeId(challengeId)
        val useCase = PutRecordUseCaseImpl(repo)
        useCase(record).last()
    }

    private fun showCompleteRecordDialog(clearRecord: String) {
        val dlgBinding =
            LayoutCompleteBinding.inflate(LayoutInflater.from(this@ChallengePlayActivity))
        dlgBinding.tvRecord.text = clearRecord
        dlgBinding.lottieAnim.playAnimation()
        MaterialAlertDialogBuilder(this@ChallengePlayActivity)
            .setView(dlgBinding.root)
            .setNegativeButton(R.string.confirm) { _, _ -> finish() }
            .setNeutralButton(R.string.show_replay) { _, _ -> replay() }
            .setCancelable(false)
            .show()
    }

    private fun replay() {
        replaceFragment(R.id.rootLayout, SudokuHistoryFragment.new())
        gamePlayViewModel.backToStartingMatrix()
        realServerTimer.pass(0)
        recordViewModel.playCapturedHistory()
    }
}