package kr.co.hs.sudoku.feature.play

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityPlayBattleBinding
import kr.co.hs.sudoku.databinding.LayoutCompleteBinding
import kr.co.hs.sudoku.extension.NumberExtension.toTimerFormat
import kr.co.hs.sudoku.extension.platform.ActivityExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.removeFragment
import kr.co.hs.sudoku.extension.platform.ActivityExtension.replaceFragment
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.ContextExtension.getDrawableCompat
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.history.impl.HistoryQueueImpl
import kr.co.hs.sudoku.repository.ProfileRepositoryImpl
import kr.co.hs.sudoku.repository.timer.TimerImpl
import kr.co.hs.sudoku.usecase.user.GetProfileUseCase
import kr.co.hs.sudoku.viewmodel.GamePlayViewModel
import kr.co.hs.sudoku.viewmodel.RecordViewModel

class WithCPUPlayActivity : Activity() {

    companion object {
        fun Activity.startBattlePlayActivity(matrix: IntMatrix?) {
            val intent = Intent(this, WithCPUPlayActivity::class.java)
            intent.putSudokuMatrix(matrix)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_play_battle)
        binding.lifecycleOwner = this

        lifecycleScope.launch {
            withStarted {
                showProgressIndicator()
                // 상대방 정보
                setupUITargetUser()
                // 내정보
                launch { setupUICurrentUser() }
                dismissProgressIndicator()

                // 사용자 게임판 Fragment 초기화
                initMatrix()
            }


            gamePlayViewModel.statusFlow.collect {
                when (it) {
                    is GamePlayViewModel.Status.OnReady -> it.onSudokuReady()
                    is GamePlayViewModel.Status.OnStart -> it.onSudokuStart()
                    is GamePlayViewModel.Status.Completed -> it.onSudokuCompleted()
                    else -> {}
                }
            }
        }

        recordViewModel.timer.observe(this) {
            binding.tvTimer.text = it
        }
    }

    private fun initMatrix() {
        getSudokuMatrix()
            .takeIf { it != null }
            ?.run {
                replaceFragment(R.id.userBoardLayout, SudokuPlayFragment.newInstance())
                gamePlayViewModel.buildSudokuMatrix(this)
            }
    }

    lateinit var binding: ActivityPlayBattleBinding


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/05/02
     * @comment 상대방 사용자 정보 표시
     **/
    private fun setupUITargetUser() = with(binding.targetUserLayout) {
        ivPhoto.load(getDrawableCompat(R.drawable.ic_computer))
        tvDisplayName.text = getString(R.string.caption_cpu)
        tvStatusMessage.visibility = View.GONE
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/05/02
     * @comment 내 정보 표시
     **/
    private suspend fun setupUICurrentUser() = with(binding.currentUserLayout) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            val profile = withContext(Dispatchers.IO) {
                GetProfileUseCase(ProfileRepositoryImpl()).invoke(uid).last()
            }

            profile.iconUrl?.run { ivPhoto.load(this) }
            tvDisplayName.text = profile.displayName
            tvStatusMessage.text = profile.message
            profile.locale?.getLocaleFlag()?.let { flag ->
                with(binding.currentUserNationFlag) {
                    visibility = View.VISIBLE
                    text = flag
                }
            }
        } ?: kotlin.run {
            ivPhoto.load(getDrawableCompat(R.drawable.ic_person))
            tvDisplayName.text = getString(R.string.me)
            tvStatusMessage.visibility = View.GONE
        }
    }

    private val gamePlayViewModel: GamePlayViewModel by lazy { gamePlayViewModels() }

    private fun GamePlayViewModel.Status.OnReady.onSudokuReady() {
        if (recordViewModel.isRunningCapturedHistoryEvent())
            return

        replaceFragment(R.id.targetBoardLayout, SudokuAutoPlayFragment.new(matrix))
    }

    private fun GamePlayViewModel.Status.OnStart.onSudokuStart() {
        if (recordViewModel.isRunningCapturedHistoryEvent())
            return

        with(recordViewModel) {
            bind(stage)
            setTimer(TimerImpl())
            setHistoryWriter(HistoryQueueImpl())
            play()
        }
    }

    private val recordViewModel: RecordViewModel by lazy { recordViewModels() }

    private fun GamePlayViewModel.Status.Completed.onSudokuCompleted() {
        with(recordViewModel) {
            if (isRunningCapturedHistoryEvent()) {
                stopCapturedHistory()
            } else {
                stop()
                if (stage.isSudokuClear() && stage.getClearTime() >= 0) {
                    showCompleteRecordDialog(stage.getClearTime())
                } else {
                    showLostDialog()
                }
            }
        }
    }


    private var completeDialog: Dialog? = null
    private fun showCompleteRecordDialog(clearRecord: Long) {
        val dlg = completeDialog
        if (dlg == null || !dlg.isShowing) {
            val dlgBinding =
                LayoutCompleteBinding.inflate(LayoutInflater.from(this))
            dlgBinding.tvRecord.text = clearRecord.toTimerFormat()
            dlgBinding.lottieAnim.playAnimation()
            completeDialog = MaterialAlertDialogBuilder(this)
                .setView(dlgBinding.root)
                .setNegativeButton(R.string.confirm) { _, _ -> finish() }
                .setNeutralButton(R.string.show_replay) { _, _ -> replay() }
                .setPositiveButton(R.string.retry) { _, _ -> retry() }
                .setCancelable(false)
                .show()
        }
    }

    private fun replay() {
        removeFragment(SudokuAutoPlayFragment::class.java)
        replaceFragment(R.id.userBoardLayout, SudokuHistoryFragment.newInstance())
        recordViewModel.playCapturedHistory()
        gamePlayViewModel.backToStartingMatrix()
    }

    private fun retry() {
        initMatrix()
        recordViewModel.stop()
        binding.tvTimer.text = 0L.toTimerFormat()
    }

    private fun showLostDialog() {
        val dlg = completeDialog
        if (dlg == null || !dlg.isShowing) {
            val dlgBinding =
                LayoutCompleteBinding.inflate(LayoutInflater.from(this))
            dlgBinding.tvRecord.isVisible = false
            dlgBinding.tvRecordTitle.isVisible = false
            dlgBinding.tvCongratulation.text = getString(R.string.lost_message)
            completeDialog = MaterialAlertDialogBuilder(this)
                .setView(dlgBinding.root)
                .setNegativeButton(R.string.confirm) { _, _ -> finish() }
                .setPositiveButton(R.string.retry) { _, _ -> retry() }
                .setCancelable(false)
                .show()
        }


    }
}