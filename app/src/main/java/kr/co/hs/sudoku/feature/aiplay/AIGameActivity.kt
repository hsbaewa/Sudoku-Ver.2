package kr.co.hs.sudoku.feature.aiplay

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewTreeObserver
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityPlayMultiBinding
import kr.co.hs.sudoku.databinding.LayoutCompleteBinding
import kr.co.hs.sudoku.extension.CoilExt.load
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.NumberExtension.toTimerFormat
import kr.co.hs.sudoku.extension.platform.ActivityExtension.isShowProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showSnackBar
import kr.co.hs.sudoku.extension.platform.ContextExtension.getDrawableCompat
import kr.co.hs.sudoku.feature.battle.MultiPlayViewerStageFragment
import kr.co.hs.sudoku.feature.single.SinglePlayControlStageFragment
import kr.co.hs.sudoku.feature.single.SinglePlayViewModel
import kr.co.hs.sudoku.feature.stage.StageFragment
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.matrix.EmptyMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.history.impl.HistoryQueueImpl
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.parcel.MatrixParcelModel
import kr.co.hs.sudoku.repository.timer.TimerImpl
import kr.co.hs.sudoku.usecase.AutoGenerateSudokuUseCase
import kr.co.hs.sudoku.usecase.PlaySudokuUseCaseImpl
import kr.co.hs.sudoku.viewmodel.RecordViewModel

class AIGameActivity : Activity(), IntCoordinateCellEntity.ValueChangedListener {
    companion object {
        private const val EXTRA_MATRIX = "EXTRA_MATRIX"
        fun newIntent(context: Context, matrix: IntMatrix) =
            Intent(context, AIGameActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(EXTRA_MATRIX, MatrixParcelModel(matrix))

        fun start(context: Context, matrix: IntMatrix) =
            context.startActivity(newIntent(context, matrix))
    }

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
    private val binding: ActivityPlayMultiBinding
            by lazy { DataBindingUtil.setContentView(this, R.layout.activity_play_multi) }
    private val recordViewModel: RecordViewModel by viewModels()
    private val singlePlayViewModel: SinglePlayViewModel by viewModels {
        SinglePlayViewModel.ProviderFactory(startingMatrix)
    }
    private var lastKnownUserProfile: ProfileEntity? = null
    private val currentUserUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw Exception("사용자 인증 정보가 없습니다. 게임 진행을 위해서는 먼저 사용자 인증이 필요합니다.")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        onBackPressedDispatcher.addCallback { showExitDialog() }

        binding.layoutUser.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.layoutUser.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val w = binding.layoutUser.measuredWidth
                    binding.layoutUser.layoutParams.height = w + 130.dp.toInt()
                }
            }
        )

        lifecycleScope.launch {
            val userProfile = withContext(Dispatchers.IO) { getProfile(currentUserUid) }
            setUserProfile(userProfile)

            with(binding) {
                ivEnemyIcon.load(getDrawableCompat(R.drawable.ic_computer))
                tvEnemyDisplayName.text = getString(R.string.caption_cpu)
                tvEnemyMessage.isVisible = false
            }
        }


        with(supportFragmentManager.beginTransaction()) {
            replace(R.id.layout_user, playerFragment)
            replace(R.id.layout_enemy, aiFragment)
            commit()
        }


        recordViewModel.timer.observe(this) { binding.tvTimer.text = it }
        with(singlePlayViewModel) {
            error.observe(this@AIGameActivity) { showSnackBar(it.message.toString()) }
            isRunningProgress.observe(this@AIGameActivity) { isShowProgressIndicator = it }
            command.observe(this@AIGameActivity) {
                when (it) {
                    is SinglePlayViewModel.Started -> {
                        with(recordViewModel) {
                            playerFragment.bindStage(this)
                            setTimer(TimerImpl())
                            setHistoryWriter(HistoryQueueImpl())
                        }
                        recordViewModel.play()
                        startPlayAI()
                    }

                    else -> {}
                }
            }
        }
    }

    private val playerFragment: SinglePlayControlStageFragment by lazy {
        StageFragment.newInstance<SinglePlayControlStageFragment>(startingMatrix)
            .apply { setValueChangedListener(this@AIGameActivity) }
    }

    private val aiFragment: MultiPlayViewerStageFragment by lazy {
        StageFragment.newInstance<MultiPlayViewerStageFragment>(startingMatrix)
            .apply { setValueChangedListener(this@AIGameActivity) }
    }

    override fun onChanged(cell: IntCoordinateCellEntity) {
        when {
            playerFragment.isCleared() && recordViewModel.isRunningCapturedHistoryEvent() ->
                recordViewModel.stopCapturedHistory()

            playerFragment.isCleared() && !recordViewModel.isRunningCapturedHistoryEvent() -> {
                recordViewModel.stop()
                val record = playerFragment.getClearTime()
                showCompleteRecordDialog(record)
                stopPlayAI()
            }

            aiFragment.isCleared() -> {
                recordViewModel.stop()
                showLostDialog()
            }
        }
    }


    private var playAIJob: Job? = null
    private fun startPlayAI() {
        playAIJob = lifecycleScope.launch {
            val stage = withContext(Dispatchers.IO) {
                val useCase = AutoGenerateSudokuUseCase(
                    startingMatrix.boxSize,
                    startingMatrix.boxCount,
                    startingMatrix
                )
                useCase().last()
            }
            aiFragment.setStatus(true, null)
            aiFragment.setValues(stage.toValueTable())
            val playUseCase = PlaySudokuUseCaseImpl(stage = stage, 2000)
            playUseCase().collect {
                val value = it.runCatching { getValue() }.getOrDefault(0)
                aiFragment.setValue(it.row, it.column, value)
            }
        }
    }

    private fun stopPlayAI() {
        playAIJob?.cancel()
    }

    fun viewerBoard(on: (MultiPlayViewerStageFragment) -> Unit) = with(supportFragmentManager) {
        val tag = MultiPlayViewerStageFragment::class.java.simpleName
        findFragmentByTag(tag)?.run { this as? MultiPlayViewerStageFragment }
            ?.also(on)
            ?: with(beginTransaction()) {
                val fragment =
                    StageFragment.newInstance<MultiPlayViewerStageFragment>(startingMatrix)
                replace(R.id.layout_enemy, fragment, tag).runOnCommit { on(fragment) }
            }.commit()
    }

    private fun releaseViewerBoard() = with(supportFragmentManager) {
        findFragmentByTag(MultiPlayViewerStageFragment::class.java.simpleName)
            ?.also { with(beginTransaction()) { remove(it) }.commit() }
    }

    fun stopTimer() = recordViewModel.stop()

    private fun setUserProfile(profile: ProfileEntity?) = if (profile != null) {
        profile.takeIf { it != lastKnownUserProfile }
            ?.run {
                profile.run {
                    binding.ivUserIcon.load(
                        iconUrl,
                        errorIcon = getDrawableCompat(R.drawable.ic_person)
                    )
                    binding.tvUserDisplayName.text = displayName
                    binding.tvUserMessage.isVisible = message?.isNotEmpty() == true
                    binding.tvUserMessage.text = message
                }
                lastKnownUserProfile = this
            }
    } else {
        with(binding) {
            ivUserIcon.setImageDrawable(null)
            tvUserDisplayName.text = null
            tvUserMessage.text = null
        }

        lastKnownUserProfile = null
    }

    private fun showCompleteRecordDialog(clearRecord: Long) {
        val dlgBinding =
            LayoutCompleteBinding.inflate(LayoutInflater.from(this))
        dlgBinding.tvRecord.text = clearRecord.toTimerFormat()
        dlgBinding.lottieAnim.playAnimation()
        MaterialAlertDialogBuilder(this)
            .setView(dlgBinding.root)
            .setPositiveButton(R.string.confirm) { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.exit_confirm_for_single)
            .setPositiveButton(R.string.confirm) { _, _ -> finish() }
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(false)
            .show()
    }


    private var completeDialog: Dialog? = null
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

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showRetryPopup() =
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.retry_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.confirm) { _, _ -> retry() }
            .setCancelable(false)
            .show()


    private fun retry() {
        recordViewModel.stop()
        binding.tvTimer.text = 0L.toTimerFormat()
        singlePlayViewModel.initMatrix()
        stopPlayAI()
        aiFragment.setStatus(false, null)
        aiFragment.initBoard()
    }
}