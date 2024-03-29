package kr.co.hs.sudoku.feature.multi.play

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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityPlayMultiBinding
import kr.co.hs.sudoku.databinding.LayoutCompleteBinding
import kr.co.hs.sudoku.extension.CoilExt.loadProfileImage
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.NumberExtension.toTimerFormat
import kr.co.hs.sudoku.extension.platform.ActivityExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.isShowProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showSnackBar
import kr.co.hs.sudoku.feature.profile.UserProfileViewModel
import kr.co.hs.sudoku.feature.multi.dashboard.MultiDashboardViewModel
import kr.co.hs.sudoku.feature.single.play.SinglePlayControlStageFragment
import kr.co.hs.sudoku.feature.single.play.SinglePlayViewModel
import kr.co.hs.sudoku.feature.stage.StageFragment
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.matrix.EmptyMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.model.stage.history.impl.HistoryQueueImpl
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.parcel.MatrixParcelModel
import kr.co.hs.sudoku.repository.timer.TimerImpl
import kr.co.hs.sudoku.usecase.AutoGenerateSudokuUseCase
import kr.co.hs.sudoku.usecase.PlaySudokuUseCaseImpl
import kr.co.hs.sudoku.viewmodel.RecordViewModel
import kr.co.hs.sudoku.viewmodel.ViewModel

@AndroidEntryPoint
class MultiPlayWithAIActivity : Activity(), IntCoordinateCellEntity.ValueChangedListener {
    companion object {
        private const val EXTRA_MATRIX = "EXTRA_MATRIX"
        fun newIntent(context: Context, matrix: IntMatrix) =
            Intent(context, MultiPlayWithAIActivity::class.java)
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
    private val app: App by lazy { applicationContext as App }
    private val userProfileViewModel: UserProfileViewModel by viewModels()
    private val multiDashboardViewModel: MultiDashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        onBackPressedDispatcher.addCallback { showExitDialog() }

        recordViewModel.timer.observe(this) { binding.tvTimer.text = it }

        with(binding) {
            ivEnemyIcon.loadProfileImage("", R.drawable.ic_computer)
            tvEnemyName.text = getString(R.string.caption_cpu)
            tvEnemyGrade.isVisible = false
            toolBarEnemy.isVisible = false
            tvEnemyFlag.isVisible = false
        }


        with(userProfileViewModel) {
            error.observe(this@MultiPlayWithAIActivity) { it.showErrorAlert() }
            isRunningProgress.observe(this@MultiPlayWithAIActivity) { isShowProgressIndicator = it }
            profile.observe(this@MultiPlayWithAIActivity) { profile ->
                profile
                    ?.run {
                        setUserProfile(this)
                    }
                    ?: run {
                        with(binding) {
                            ivUserIcon.loadProfileImage("", R.drawable.ic_person)
                            tvUserName.text = getString(R.string.me)
                            tvUserGrade.isVisible = false
                            tvUserFlag.isVisible = false
                        }
                    }
            }

            requestLastUserProfile()
        }

        with(singlePlayViewModel) {
            error.observe(this@MultiPlayWithAIActivity) { showSnackBar(it.message.toString()) }
            isRunningProgress.observe(this@MultiPlayWithAIActivity) { isShowProgressIndicator = it }
            command.observe(this@MultiPlayWithAIActivity) {
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

        binding.layoutUser.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.layoutUser.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val w = binding.layoutUser.measuredWidth
                    binding.layoutUser.layoutParams.height = w + 80.dp.toInt()

                    with(supportFragmentManager.beginTransaction()) {
                        replace(R.id.layout_user, playerFragment)
                        replace(R.id.layout_enemy, aiFragment)
                        runOnCommit {
                            initStageForAI()
                        }
                        commit()
                    }

                }
            }
        )

    }


    private val playerFragment: SinglePlayControlStageFragment by lazy {
        StageFragment.newInstance<SinglePlayControlStageFragment>(startingMatrix)
            .apply { setValueChangedListener(this@MultiPlayWithAIActivity) }
    }

    private val aiFragment: MultiPlayViewerStageFragment by lazy {
        StageFragment.newInstance<MultiPlayViewerStageFragment>(startingMatrix)
            .apply { setValueChangedListener(this@MultiPlayWithAIActivity) }
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

    private lateinit var stage: Stage
    private fun initStageForAI() = lifecycleScope.launch {
        showProgressIndicator()
        stage = withContext(Dispatchers.IO) {
            val useCase = AutoGenerateSudokuUseCase(
                startingMatrix.boxSize,
                startingMatrix.boxCount,
                startingMatrix
            )
            useCase().last()
        }

        dismissProgressIndicator()
    }

    private var playAIJob: Job? = null
    private fun startPlayAI() {
        playAIJob = lifecycleScope.launch {
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

    @Suppress("unused")
    fun stopTimer() = recordViewModel.stop()

    private fun setUserProfile(profile: ProfileEntity?) = if (profile != null) {
        profile.takeIf { it != lastKnownUserProfile }
            ?.run {
                profile.run {
                    binding.ivUserIcon.loadProfileImage(iconUrl, R.drawable.ic_person)
                    binding.tvUserName.text = displayName
                    multiDashboardViewModel.requestStatistics(uid) {
                        binding.tvUserGrade.text = when (it) {
                            is ViewModel.OnError -> getString(R.string.loading_statistics)
                            is ViewModel.OnStart -> getString(R.string.loading_statistics)
                            is ViewModel.OnFinish -> getString(
                                R.string.format_statistics_with_ranking,
                                it.d.winCount,
                                it.d.playCount - it.d.winCount,
                                when (it.d.ranking) {
                                    0L -> getString(R.string.rank_format_nan)
                                    1L -> getString(R.string.rank_format_first)
                                    2L -> getString(R.string.rank_format_second)
                                    3L -> getString(R.string.rank_format_third)
                                    else -> getString(R.string.rank_format, it.d.ranking)
                                }
                            )
                        }
                        binding.tvUserGrade.isVisible = true
                    }

                    with(binding.tvUserFlag) {
                        locale?.getLocaleFlag()
                            ?.let { flag ->
                                text = flag
                                isVisible = true
                            }
                            ?: run { isVisible = false }
                    }
                }
                lastKnownUserProfile = this
            }
    } else {
        with(binding) {
            ivUserIcon.setImageDrawable(null)
            tvUserName.text = null
            tvUserFlag.isVisible = false
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