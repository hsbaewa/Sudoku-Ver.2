package kr.co.hs.sudoku.feature.battle

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withStarted
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityPlayBattleBinding
import kr.co.hs.sudoku.databinding.LayoutCompleteBinding
import kr.co.hs.sudoku.databinding.LayoutItemUserBinding
import kr.co.hs.sudoku.extension.NumberExtension.toTimerFormat
import kr.co.hs.sudoku.extension.platform.ActivityExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.hasFragment
import kr.co.hs.sudoku.extension.platform.ActivityExtension.removeFragment
import kr.co.hs.sudoku.extension.platform.ActivityExtension.replaceFragment
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showSnackBar
import kr.co.hs.sudoku.extension.platform.ContextExtension.getDrawableCompat
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.battle.BattleParticipantEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.timer.BattleTimer
import kr.co.hs.sudoku.viewmodel.BattlePlayViewModel
import kr.co.hs.sudoku.viewmodel.RecordViewModel

class BattlePlayActivity : Activity() {
    companion object {
        private fun Activity.newIntent(uid: String, battleId: String) =
            Intent(this, BattlePlayActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putUserId(uid)
                .putBattleId(battleId)

        fun Activity.startBattlePlayActivity(uid: String, battleId: String) {
            startActivity(newIntent(uid, battleId))
        }
    }

    lateinit var binding: ActivityPlayBattleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_play_battle)
        binding.lifecycleOwner = this

        //--------------------------------------------------------------------------------------------\\
        //----------------------------------------- setup UI -----------------------------------------\\
        //--------------------------------------------------------------------------------------------\\

        binding.tvTimer.setupUITimer(recordViewModel.timer)
        binding.btnEject.setupUIExitButton()

        //--------------------------------------------------------------------------------------------\\
        //----------------------------------------- observe ------------------------------------------\\
        //--------------------------------------------------------------------------------------------\\

        battleViewModel.let {
            it.currentProfile.observe(this, observerForCurrentUser)
            it.participantList.observe(this, observerForParticipant)
            it.error.observe(this, observerForError)
            it.isRunningProgress.observe(this, observerForProgress)
            it.isClearSudokuRecord.observe(this) { record ->
                if (record >= 0) {
                    showCompleteRecordDialog(record)
                }
            }
        }


        //--------------------------------------------------------------------------------------------\\
        //----------------------------------------- bind event flow --------------------------------------\\
        //--------------------------------------------------------------------------------------------\\
        lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            throwable.message?.run { showSnackBar(this) }
        }) {
            withStarted {
                val uid =
                    getUserId() ?: throw Exception(getString(R.string.error_require_authenticate))
                battleViewModel.init(app.getBattleRepository(), uid)
                val battleId =
                    getBattleId() ?: throw Exception(getString(R.string.error_require_authenticate))
                battleViewModel.join(battleId, app.getProfileRepository(), uid)
            }

            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val uid =
                    getUserId() ?: throw Exception(getString(R.string.error_require_authenticate))
                battleViewModel.getEventFlow(uid).collect {
                    when (it) {
                        is BattlePlayViewModel.Event.OnStarted -> it.onStart()
                        is BattlePlayViewModel.Event.OnCleared -> onCleared()
                        else -> {}
                    }
                }
            }

        }

        onBackPressedDispatcher.addCallback {
            if (recordViewModel.isRunningTimer()) {
                showExitDialog()
            } else {
                getUserId()?.run { battleViewModel.exit(this) { finish() } }
            }
        }
    }

    // play ViewModel
    private val battleViewModel: BattlePlayViewModel by viewModels()

    // timer ViewModel
    private val realServerTimer by lazy { BattleTimer() }
    private val recordViewModel: RecordViewModel by lazy { recordViewModels() }

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- observer -----------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private val observerForCurrentUser = Observer<BattleParticipantEntity?> {
        setCurrentUserProfile(it)
        setCurrentUserStage(it?.uid)
    }

    private val observerForParticipant = Observer<List<BattleParticipantEntity>> {
        val participantProfile = getUserId()?.let { uid -> it.find { it.uid != uid } }
        val isCleared = getUserId()
            ?.let { uid -> battleViewModel.getStage(uid)?.isSudokuClear() ?: false } ?: false

        if (!isCleared) {
            setParticipantUserProfile(participantProfile)
            setParticipateUserStage(participantProfile?.uid)
        }
    }

    private val observerForError = Observer<Throwable> {
        it.message?.run { showSnackBar(this) }
    }

    private val observerForProgress = Observer<Boolean> {
        it.takeIf { it }?.run { showProgressIndicator() } ?: dismissProgressIndicator()
    }


    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- flow event ----------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private suspend fun BattlePlayViewModel.Event.OnStarted.onStart() {
        if (battle is BattleEntity.RunningBattleEntity) {
            realServerTimer.initTime(battle.startedAt)
        } else if (battle is BattleEntity.ClearedBattleEntity) {
            realServerTimer.initTime(battle.startedAt)
        }
        recordViewModel.bind(stage)
        recordViewModel.setTimer(realServerTimer)
        battleViewModel.isClearSudokuRecord.value
            ?.takeIf { it == -1L }
            ?.run { recordViewModel.play() }
    }

    private fun onCleared() {
        recordViewModel.stop()
    }

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- setup Board Fragment -------------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private fun setCurrentUserStage(uid: String?) {
        uid
            ?.run {
                if (!hasFragment(BattlePlayFragment::class.java)) {
                    replaceFragment(R.id.userBoardLayout, BattlePlayFragment.new(this))
                }
            } ?: removeFragment(BattlePlayFragment::class.java)
    }

    private fun setParticipateUserStage(uid: String?) {
        uid
            ?.run {
                if (!hasFragment(BattleParticipantFragment::class.java)) {
                    replaceFragment(R.id.targetBoardLayout, BattleParticipantFragment.new(this))
                }
            } ?: removeFragment(BattleParticipantFragment::class.java)
    }

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- Participant Profile ------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private fun setCurrentUserProfile(profile: ProfileEntity?) {
        binding.currentUserNationFlag.setupUINationalFlag(profile)
        binding.currentUserLayout.setupUIProfile(profile)
    }

    private fun setParticipantUserProfile(profile: ProfileEntity?) {
        binding.targetUserNationFlag.setupUINationalFlag(profile)
        binding.targetUserLayout.setupUIProfile(profile)
    }


    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- setup UI -----------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private fun ImageButton.setupUIExitButton() {
        setOnClickListener {
            if (recordViewModel.isRunningTimer()) {
                showExitDialog()
            } else {
                getUserId()?.run { battleViewModel.exit(this) { finish() } }
            }
        }
    }

    private fun TextView.setupUINationalFlag(profile: ProfileEntity?) {
        text = profile?.locale?.getLocaleFlag()
    }

    private fun LayoutItemUserBinding.setupUIProfile(profile: ProfileEntity?) {
        profile?.run {
            ivPhoto.load(iconUrl, errorIcon = getDrawableCompat(R.drawable.ic_person))
            tvDisplayName.text = displayName
            tvStatusMessage.text = message
        } ?: kotlin.run {
            ivPhoto.setImageDrawable(null)
            tvDisplayName.text = null
            tvStatusMessage.text = null
        }
    }

    private fun TextView.setupUITimer(data: LiveData<String>) {
        data.observe(this@BattlePlayActivity) { text = it }
    }


    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- Dialog ------------------------------------------\\
    //--------------------------------------------------------------------------------------------\\

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
            .setMessage(R.string.battle_exit_message)
            .setPositiveButton(R.string.confirm) { _, _ ->
                getUserId()?.run { battleViewModel.exit(this) { finish() } }
            }
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(false)
            .show()
    }
}