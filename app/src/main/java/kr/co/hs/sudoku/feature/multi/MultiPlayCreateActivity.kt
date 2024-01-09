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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.LayoutCreateMultiPlayBinding
import kr.co.hs.sudoku.extension.FirebaseCloudMessagingExt.subscribeBattle
import kr.co.hs.sudoku.extension.platform.ActivityExtension.isShowProgressIndicator
import kr.co.hs.sudoku.feature.multi.play.MultiPlayWithAIActivity
import kr.co.hs.sudoku.feature.matrixlist.MatrixListViewModel
import kr.co.hs.sudoku.feature.matrixlist.MatrixSelectBottomSheetFragment
import kr.co.hs.sudoku.feature.multi.play.MultiPlayActivity
import kr.co.hs.sudoku.feature.multi.play.MultiPlayViewModel

class MultiPlayCreateActivity : Activity() {
    companion object {
        private fun newIntent(context: Context) =
            Intent(context, MultiPlayCreateActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

        fun start(context: Context) = context.startActivity(newIntent(context))
    }

    private val binding: LayoutCreateMultiPlayBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.layout_create_multi_play)
    }
    private val matrixListViewModel: MatrixListViewModel by viewModels()
    private val multiPlayViewModel: MultiPlayViewModel by viewModels {
        val app = applicationContext as App
        MultiPlayViewModel.ProviderFactory(app.getBattleRepository())
    }

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
                matrix = it
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
                            val registrationRepository =
                                (applicationContext as App).getRegistrationRepository()
                            val hasSeen =
                                runBlocking { registrationRepository.hasSeenNotificationParticipate() }

                            if (hasSeen) {
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
                    if (binding.checkboxNotificationParticipant.isChecked) {
                        FirebaseMessaging.getInstance().subscribeBattle(it)
                    }
                    startMultiPlay(it.id)
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

    private fun startMultiPlay(battleId: String?) = battleId?.let {
        lifecycleScope
            .launch { startActivity(MultiPlayActivity.newIntent(this@MultiPlayCreateActivity, it)) }
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
                    runBlocking {
                        val app = applicationContext as App
                        app.getRegistrationRepository().seenNotificationParticipate()
                    }
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
}