package kr.co.hs.sudoku.feature.multi.play

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityPlayMultiBinding
import kr.co.hs.sudoku.databinding.LayoutCompleteBinding
import kr.co.hs.sudoku.extension.CoilExt.loadProfileImage
import kr.co.hs.sudoku.extension.FirebaseCloudMessagingExt.unsubscribeBattle
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.NumberExtension.toTimerFormat
import kr.co.hs.sudoku.extension.platform.ActivityExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.isShowProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showProgressIndicator
import kr.co.hs.sudoku.feature.ad.NativeAdFragment
import kr.co.hs.sudoku.feature.multi.dashboard.MultiDashboardViewModel
import kr.co.hs.sudoku.feature.stage.StageFragment
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.battle.ParticipantEntity
import kr.co.hs.sudoku.model.matrix.EmptyMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.timer.BattleTimer
import kr.co.hs.sudoku.viewmodel.RecordViewModel
import kr.co.hs.sudoku.viewmodel.ViewModel
import org.jetbrains.annotations.TestOnly

class MultiPlayActivity : Activity(), IntCoordinateCellEntity.ValueChangedListener {
    companion object {
        private const val EXTRA_BATTLE_ID = "kr.co.hs.sudoku.EXTRA_BATTLE_ID"
        fun newIntent(context: Context, battleId: String) =
            Intent(context, MultiPlayActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(EXTRA_BATTLE_ID, battleId)

        fun start(context: Context, battleId: String) =
            context.startActivity(newIntent(context, battleId))
    }

    private val binding: ActivityPlayMultiBinding
            by lazy { DataBindingUtil.setContentView(this, R.layout.activity_play_multi) }
    private val battleViewModel: MultiPlayViewModel by viewModels {
        val app = applicationContext as App
        MultiPlayViewModel.ProviderFactory(app.getBattleRepository())
    }
    private val recordViewModel: RecordViewModel by viewModels()
    private val realServerTimer by lazy { BattleTimer() }

    private val currentUserUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw Exception("사용자 인증 정보가 없습니다. 게임 진행을 위해서는 먼저 사용자 인증이 필요합니다.")

    private var lastKnownUserProfile: ProfileEntity? = null
    private var lastKnownOpponentProfile: ProfileEntity? = null

    private var isAlreadyPending = false

    @TestOnly
    var isShowErrorDialog = true

    private val multiDashboardViewModel: MultiDashboardViewModel by viewModels {
        val app = applicationContext as App
        MultiDashboardViewModel.ProviderFactory(app.getBattleRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recordViewModel.timer.observe(this) { binding.tvTimer.text = it }
        with(battleViewModel) {
            isRunningProgress.observe(this@MultiPlayActivity) { isShowProgressIndicator = it }
            error.observe(this@MultiPlayActivity) { it.showMultiPlayError() }
            battleEntity.observe(this@MultiPlayActivity) { onBattleEntity(it) }
            intent.getStringExtra(EXTRA_BATTLE_ID)?.run { startEventMonitoring(this) }
        }


        binding.layoutUser.viewTreeObserver.addOnGlobalLayoutListener(
            object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.layoutUser.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val w = binding.layoutUser.measuredWidth
                    binding.layoutUser.layoutParams.height = w + 80.dp.toInt()
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.play_multi, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_exit -> {
                showExitDialog()
                true
            }

            android.R.id.home -> {
                navigateUpToParent()
                true
            }

            else -> false
        }
    }


    private fun onBattleEntity(battleEntity: BattleEntity) {
        Log.d("hsbaewa", "onBattleEntity($battleEntity)")
        when (battleEntity) {
            is BattleEntity.Opened,
            is BattleEntity.Playing,
            is BattleEntity.Closed -> {

                val user = battleEntity.participants.find { it.uid == currentUserUid }
                onCurrentUser(user)
                val opponent = battleEntity.participants.find { it.uid != currentUserUid }
                onOpponentUser(opponent)

                if (battleEntity is BattleEntity.Playing) {
                    startTimer(battleEntity)
                }

            }

            is BattleEntity.Pending -> if (!isAlreadyPending && battleEntity.isGeneratedSudoku) {
                isAlreadyPending = true
                controlBoard {
                    it.startCountDown {
                        battleViewModel
                            .takeIf { vm -> vm.isHost() }
                            ?.start()
                    }
                    it.setStatus(false, null)
                }
                viewerBoard {
                    it.setStatus(false, null)
                }
            }

            is BattleEntity.Invalid -> finish()

            else -> {}
        }
    }

    private fun onCurrentUser(participant: ParticipantEntity?) {
        setUserProfile(participant)
        when (participant) {
            is ParticipantEntity.Host,
            is ParticipantEntity.Guest,
            is ParticipantEntity.ReadyGuest,
            is ParticipantEntity.Playing,
            is ParticipantEntity.Cleared -> controlBoard { it.setStatus(participant) }

            else -> {
                releaseControlBoard()
                finish()
            }
        }
    }

    private fun onOpponentUser(participant: ParticipantEntity?) {
        setOpponentProfile(participant)
        when (participant) {
            is ParticipantEntity.Host,
            is ParticipantEntity.Guest,
            is ParticipantEntity.ReadyGuest,
            is ParticipantEntity.Playing,
            is ParticipantEntity.Cleared -> viewerBoard { it.setStatus(participant) }

            else -> {
                releaseViewerBoard()
                if (!isVisibleNativeAd()) {
                    showNativeAd()
                }
            }
        }
    }


    fun controlBoard(on: (MultiPlayControlStageFragment) -> Unit) =
        with(supportFragmentManager) {
            val tag = MultiPlayControlStageFragment::class.java.simpleName
            findFragmentByTag(tag)?.run { this as? MultiPlayControlStageFragment }
                ?.also(on)
                ?: with(beginTransaction()) {
                    val fragment =
                        StageFragment.newInstance<MultiPlayControlStageFragment>(startingMatrix)
                    fragment.setValueChangedListener(this@MultiPlayActivity)
                    replace(R.id.layout_user, fragment, tag).runOnCommit { on(fragment) }
                }.commit()
        }


    private fun releaseControlBoard() = with(supportFragmentManager) {
        findFragmentByTag(MultiPlayControlStageFragment::class.java.simpleName)
            ?.also { with(beginTransaction()) { remove(it) }.commit() }
    }


    fun viewerBoard(on: (MultiPlayViewerStageFragment) -> Unit) =
        with(supportFragmentManager) {
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

    fun startTimer(playingBattleEntity: BattleEntity.Playing?) {
        lifecycleScope.launch {

            playingBattleEntity?.playedAt?.let { date ->
                withContext(Dispatchers.IO) { realServerTimer.initTime(date) }
            }

            recordViewModel.apply {
                controlBoard {
                    it.bindStage(this)
                    setTimer(realServerTimer)
                    if (!isRunningTimer() && !it.isCleared()) {
                        play()
                    }
                }

            }
        }
    }

    fun stopTimer() = recordViewModel.stop()

    override fun onChanged(cell: IntCoordinateCellEntity) {
        controlBoard {
            if (it.isCleared()) {
                stopTimer()


                val record = it.getClearTime()
                battleViewModel.clear(record)
                showCompleteRecordDialog(record)
            }
        }
    }


    var startingMatrix: IntMatrix = EmptyMatrix()
        get() {
            return if (field is EmptyMatrix)
                battleViewModel.battleEntity.value?.startingMatrix ?: EmptyMatrix()
            else
                field
        }
        @TestOnly
        set(value) {
            field = value
        }


    fun setUserProfile(profile: ProfileEntity?) = if (profile != null) {
        profile.takeIf { it.uid != lastKnownUserProfile?.uid }
            ?.run {
                profile.run {
                    binding.ivUserIcon.loadProfileImage(iconUrl, R.drawable.ic_person)
                    binding.tvUserName.text = displayName
                    multiDashboardViewModel.requestStatistics(uid) {
                        binding.tvUserGrade.text = when (it) {
                            is ViewModel.OnStart -> getString(R.string.loading_statistics)
                            is ViewModel.OnError -> getString(R.string.error_statistics)
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
                    }
                }
                lastKnownUserProfile = this
            }
    } else {
        with(binding) {
            ivUserIcon.setImageDrawable(null)
            tvUserName.text = null
            binding.tvUserGrade.text = null
        }

        lastKnownUserProfile = null
    }

    fun setOpponentProfile(profile: ProfileEntity?) = if (profile != null) {
        profile
            .takeIf { it.uid != lastKnownOpponentProfile?.uid }
            ?.run {
                profile.run {
                    binding.ivEnemyIcon.loadProfileImage(iconUrl, R.drawable.ic_person)
                    binding.tvEnemyName.text = displayName
                    multiDashboardViewModel.requestStatistics(uid) {
                        binding.tvEnemyGrade.text = when (it) {
                            is ViewModel.OnStart -> getString(R.string.loading_statistics)
                            is ViewModel.OnError -> getString(R.string.error_statistics)
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
                    }

                    with(binding.toolBarEnemy) {
                        isVisible = true
                        setOnMenuItemClickListener {
                            return@setOnMenuItemClickListener when (it.itemId) {
                                R.id.kick -> {
                                    battleViewModel.kickPlayer(uid)
                                    true
                                }

                                else -> false
                            }
                        }
                    }
                }

                lastKnownOpponentProfile = this
            }
    } else {
        with(binding) {
            ivEnemyIcon.setImageDrawable(null)
            tvEnemyName.text = null
            tvEnemyGrade.text = null
            toolBarEnemy.isVisible = false
        }

        lastKnownOpponentProfile = null
    }


    private fun showCompleteRecordDialog(clearRecord: Long) {
        val dlgBinding =
            LayoutCompleteBinding.inflate(LayoutInflater.from(this))
        dlgBinding.tvRecord.text = clearRecord.toTimerFormat()
        dlgBinding.lottieAnim.playAnimation()
        MaterialAlertDialogBuilder(this)
            .setView(dlgBinding.root)
            .setPositiveButton(R.string.confirm) { _, _ -> onEndGameClearOrExit() }
            .setCancelable(false)
            .show()
    }

    private fun onEndGameClearOrExit() =
        lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            dismissProgressIndicator()
            throwable.showMultiPlayError()
        }) {
            showProgressIndicator()
            val battle = battleViewModel.battleEntity.value
            battleViewModel.doExit()
            battle?.run { FirebaseMessaging.getInstance().unsubscribeBattle(this) }
            dismissProgressIndicator()
            navigateUpToParent()
        }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.battle_exit_message)
            .setPositiveButton(R.string.confirm) { _, _ -> onEndGameClearOrExit() }
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(false)
            .show()
    }

    private fun showNativeAd() = with(supportFragmentManager.beginTransaction()) {
        val tag = NativeAdFragment::class.java.name
        replace(R.id.layout_enemy, NativeAdFragment.newInstance(), tag)
        commit()
    }

    private fun isVisibleNativeAd() =
        supportFragmentManager.findFragmentByTag(NativeAdFragment::class.java.name) != null

    private fun Throwable.showMultiPlayError() {
        if (!isShowErrorDialog)
            return

        showErrorAlert()
    }
}