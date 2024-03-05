package kr.co.hs.sudoku.feature.multi

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.material.checkbox.MaterialCheckBox.STATE_CHECKED
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.LayoutCreateMultiPlayBinding
import kr.co.hs.sudoku.di.repositories.RegistrationRepositoryQualifier
import kr.co.hs.sudoku.extension.FirebaseCloudMessagingExt.subscribeBattle
import kr.co.hs.sudoku.extension.platform.ActivityExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.isShowProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showProgressIndicator
import kr.co.hs.sudoku.feature.multi.play.MultiPlayWithAIActivity
import kr.co.hs.sudoku.feature.matrixlist.MatrixListViewModel
import kr.co.hs.sudoku.feature.matrixlist.MatrixSelectBottomSheetFragment
import kr.co.hs.sudoku.feature.messaging.MessagingManager
import kr.co.hs.sudoku.feature.multi.play.MultiPlayActivity
import kr.co.hs.sudoku.feature.multi.play.MultiPlayViewModel
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.repository.settings.RegistrationRepository
import javax.inject.Inject

@AndroidEntryPoint
class MultiPlayCreateActivity : Activity() {
    companion object {
        private const val EXTRA_OPPONENT_UID = "EXTRA_OPPONENT_UID"
        private fun newIntent(context: Context) =
            Intent(context, MultiPlayCreateActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

        fun start(context: Context) = context.startActivity(newIntent(context))

        private fun newIntent(context: Context, uid: String) =
            Intent(context, MultiPlayCreateActivity::class.java)
                .putExtra(EXTRA_OPPONENT_UID, uid)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

        fun start(context: Context, uid: String) = context.startActivity(newIntent(context, uid))
    }

    private val binding: LayoutCreateMultiPlayBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.layout_create_multi_play)
    }
    private val matrixListViewModel: MatrixListViewModel by viewModels()
    private val multiPlayViewModel: MultiPlayViewModel by viewModels()

    @RegistrationRepositoryQualifier
    @Inject
    lateinit var registrationRepository: RegistrationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        with(binding.cardView) {
            setOnClickListener { MatrixSelectBottomSheetFragment.showAll(supportFragmentManager) }
        }

        with(binding.matrix) {
            matrixListViewModel.selection.observe(this@MultiPlayCreateActivity) {
                MatrixSelectBottomSheetFragment.dismiss(supportFragmentManager)
                setFixedCellValues(it)
                invalidate()
                if (it != null) {
                    binding.matrix.isVisible = true
                }
            }
        }

        with(binding.checkboxNotificationParticipant) {
            addOnCheckedStateChangedListener { _, state ->
                if (state == STATE_CHECKED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcherForNotification.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        with(binding.btnCreate) {
            setOnClickListener {
                matrixListViewModel.selection.value
                    ?.run {
                        if (binding.checkboxWithAi.isChecked) {
                            startWithAI()
                        } else {
                            if (runBlocking { registrationRepository.hasSeenNotificationParticipate() }) {
                                multiPlayViewModel.create(this)
                            } else {
                                showParticipateNotificationGuide()
                            }
                        }
                    }
                    ?: showAlert(
                        getString(R.string.app_name),
                        getString(R.string.require_select_stage)
                    ) {}
            }
        }

        with(multiPlayViewModel) {
            isRunningProgress.observe(this@MultiPlayCreateActivity) { isShowProgressIndicator = it }
            battleEntity.observe(this@MultiPlayCreateActivity) {
                if (it != null) {
                    onCreatedBattle(it)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigateUpToParent()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startWithAI() = lifecycleScope.launch {
        matrixListViewModel.selection.value?.let { matrix ->
            startActivity(MultiPlayWithAIActivity.newIntent(this@MultiPlayCreateActivity, matrix))
        }
    }

    private fun showParticipateNotificationGuide(onDismiss: (() -> Unit)? = null) {
        TapTargetView.showFor(
            this,
            TapTarget.forView(
                binding.checkboxNotificationParticipant,
                getString(R.string.multi_create_label_notification_joined_participant),
                getString(R.string.multi_create_label_notification_joined_participant_guide)
            ).apply {
                cancelable(false)
                transparentTarget(true)
                outerCircleColor(R.color.gray_700)
            },
            object : TapTargetView.Listener() {
                override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                    runBlocking { registrationRepository.seenNotificationParticipate() }
                    onDismiss?.invoke()
                }
            }
        )
    }


    private val launcherForNotification =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                binding.checkboxNotificationParticipant.isChecked = false
            }
        }

    private fun onCreatedBattle(battleEntity: BattleEntity) =
        lifecycleScope.launch(CoroutineExceptionHandler { _, t ->
            dismissProgressIndicator()
            t.showErrorAlert()
        }) {
            showProgressIndicator()

            if (binding.checkboxNotificationParticipant.isChecked) {
                withContext(Dispatchers.IO) {
                    FirebaseMessaging.getInstance().subscribeBattle(battleEntity).await()
                }
            }

            battleEntity.participants
                .find { participant -> participant.uid == battleEntity.host }
                ?.let { host ->
                    intent?.getStringExtra(EXTRA_OPPONENT_UID)?.let { opponentUid ->
                        MessagingManager.InviteMultiPlay(
                            battleEntity.id,
                            host.uid,
                            host.displayName,
                            opponentUid
                        )
                    }
                }
                ?.let {
                    val app = applicationContext as App
                    val messagingManager = MessagingManager(app)
                    withContext(Dispatchers.IO) { messagingManager.sendNotification(it) }
                }

            startActivity(
                MultiPlayActivity.newIntent(
                    this@MultiPlayCreateActivity,
                    battleEntity.id
                )
            )

            dismissProgressIndicator()
        }
}