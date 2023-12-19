package kr.co.hs.sudoku.feature.battle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityPlayBattleBinding
import kr.co.hs.sudoku.databinding.LayoutCompleteBinding
import kr.co.hs.sudoku.databinding.LayoutItemUserBinding
import kr.co.hs.sudoku.extension.NumberExtension.toTimerFormat
import kr.co.hs.sudoku.extension.platform.ActivityExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.ContextExtension.getDrawableCompat
import kr.co.hs.sudoku.feature.stage.StageFragment
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.battle.ParticipantEntity
import kr.co.hs.sudoku.model.matrix.EmptyMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.timer.BattleTimer
import kr.co.hs.sudoku.viewmodel.BattlePlayViewModel
import kr.co.hs.sudoku.viewmodel.RecordViewModel
import org.jetbrains.annotations.TestOnly

class BattlePlayActivity : Activity(), IntCoordinateCellEntity.ValueChangedListener {
    companion object {
        private const val EXTRA_BATTLE_ID = "kr.co.hs.sudoku.EXTRA_BATTLE_ID"
        fun newIntent(context: Context, battleId: String) =
            Intent(context, BattlePlayActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(EXTRA_BATTLE_ID, battleId)

        fun start(context: Context, battleId: String) =
            context.startActivity(newIntent(context, battleId))
    }


    lateinit var binding: ActivityPlayBattleBinding
    private val battleViewModel: BattlePlayViewModel by viewModels {
        val app = applicationContext as App
        BattlePlayViewModel.ProviderFactory(app.getBattleRepository2())
    }
    private val recordViewModel: RecordViewModel by viewModels()
    private val realServerTimer by lazy { BattleTimer() }

    private val currentUserUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw Exception("사용자 인증 정보가 없습니다. 게임 진행을 위해서는 먼저 사용자 인증이 필요합니다.")

    private var lastKnownUserProfile: ProfileEntity? = null
    private var lastKnownOpponentProfile: ProfileEntity? = null

    private var isAlreadyPending = false

    private var isExitAfterCleared = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_play_battle)
        binding.lifecycleOwner = this

        binding.btnEject.setOnClickListener { showExitDialog() }

        recordViewModel.timer.observe(this) { binding.tvTimer.text = it }

        with(battleViewModel) {
            isRunningProgress.observe(this@BattlePlayActivity) {
                it.takeIf { it }
                    ?.run { showProgressIndicator() }
                    ?: dismissProgressIndicator()
            }

            battleEntity.observe(this@BattlePlayActivity) { onBattleEntity(it) }
            startEventMonitoring(intent.getStringExtra(EXTRA_BATTLE_ID) ?: "")
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

                if (battleEntity is BattleEntity.Closed
                    && user is ParticipantEntity.Cleared
                    && isExitAfterCleared
                ) {
                    finish()
                }
            }

            is BattleEntity.Pending -> if (!isAlreadyPending && battleEntity.isGeneratedSudoku) {
                isAlreadyPending = true
                controlBoard {
                    it.startCountDown { battleViewModel.start() }
                    it.setStatus(false, null)
                }
                viewerBoard {
                    it.setStatus(false, null)
                }
            }

            BattleEntity.Invalid -> finish()

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

            else -> releaseViewerBoard()
        }
    }


    fun controlBoard(on: (MultiPlayControlStageFragment) -> Unit) = with(supportFragmentManager) {
        val tag = MultiPlayControlStageFragment::class.java.simpleName
        findFragmentByTag(tag)?.run { this as? MultiPlayControlStageFragment }
            ?.also(on)
            ?: with(beginTransaction()) {
                val fragment =
                    StageFragment.newInstance<MultiPlayControlStageFragment>(startingMatrix)
                fragment.setValueChangedListener(this@BattlePlayActivity)
                replace(R.id.userBoardLayout, fragment, tag).runOnCommit { on(fragment) }
            }.commit()
    }


    private fun releaseControlBoard() = with(supportFragmentManager) {
        findFragmentByTag(MultiPlayControlStageFragment::class.java.simpleName)
            ?.also { with(beginTransaction()) { remove(it) }.commit() }
    }


    fun viewerBoard(on: (MultiPlayViewerStageFragment) -> Unit) = with(supportFragmentManager) {
        val tag = MultiPlayViewerStageFragment::class.java.simpleName
        findFragmentByTag(tag)?.run { this as? MultiPlayViewerStageFragment }
            ?.also(on)
            ?: with(beginTransaction()) {
                val fragment =
                    StageFragment.newInstance<MultiPlayViewerStageFragment>(startingMatrix)
                replace(R.id.targetBoardLayout, fragment, tag).runOnCommit { on(fragment) }
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

    fun stopTimer() {
        recordViewModel.stop()
    }

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
        profile.takeIf { it != lastKnownUserProfile }
            ?.run {
                binding.currentUserLayout.setupUIProfile(this)
                lastKnownUserProfile = this
            }
    } else {
        binding.currentUserLayout.clearProfile()
        lastKnownUserProfile = null
    }


    private fun LayoutItemUserBinding.clearProfile() {
        ivPhoto.setImageDrawable(null)
        tvDisplayName.text = null
        tvStatusMessage.text = null
    }

    fun setOpponentProfile(profile: ProfileEntity?) = if (profile != null) {
        profile
            .takeIf { it != lastKnownOpponentProfile }
            ?.run {
                binding.targetUserLayout.setupUIProfile(this)
                lastKnownOpponentProfile = this
            }
    } else {
        binding.targetUserLayout.clearProfile()
        lastKnownOpponentProfile = null
    }


    private fun LayoutItemUserBinding.setupUIProfile(profile: ProfileEntity?) = profile
        ?.run {
            ivPhoto.load(iconUrl, errorIcon = getDrawableCompat(R.drawable.ic_person))
            tvDisplayName.text = displayName
            tvStatusMessage.text = message
        }
        ?: clearProfile()


    private fun showCompleteRecordDialog(clearRecord: Long) {
        val dlgBinding =
            LayoutCompleteBinding.inflate(LayoutInflater.from(this))
        dlgBinding.tvRecord.text = clearRecord.toTimerFormat()
        dlgBinding.lottieAnim.playAnimation()
        MaterialAlertDialogBuilder(this)
            .setView(dlgBinding.root)
            .setPositiveButton(R.string.confirm) { _, _ ->
                isExitAfterCleared = true
                battleViewModel.exit()
            }
            .setCancelable(false)
            .show()
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.battle_exit_message)
            .setPositiveButton(R.string.confirm) { _, _ ->
                if (battleViewModel.isClosedBattle()) {
                    finish()
                } else {
                    battleViewModel.exit()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(false)
            .show()
    }
}