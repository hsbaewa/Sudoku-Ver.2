package kr.co.hs.sudoku.feature.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityManageUpdatePushBinding
import kr.co.hs.sudoku.extension.platform.ActivityExtension.isShowProgressIndicator
import kr.co.hs.sudoku.feature.messaging.MessagingManager

class UpdatePushActivity : Activity() {
    companion object {
        private fun newIntent(context: Context) = Intent(context, UpdatePushActivity::class.java)
        fun start(context: Context) = context.startActivity(newIntent(context))
    }

    private val binding: ActivityManageUpdatePushBinding
            by lazy { DataBindingUtil.setContentView(this, R.layout.activity_manage_update_push) }

    private val messagingViewModel: MessagingViewModel by viewModels {
        val app = applicationContext as App
        val messagingManager = MessagingManager(app)
        MessagingViewModel.ProviderFactory(messagingManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        messagingViewModel
            .let {
                it.error.observe(this) { it.showErrorAlert() }
                it.isRunningProgress.observe(this) { isShowProgressIndicator = it }
            }

        binding.btnSend.setOnClickListener {
            showConfirm(
                R.string.admin_update_push_alert_title,
                R.string.admin_update_push_alert_message
            ) {
                if (it) {
                    lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
                        throwable.showErrorAlert()
                    }) {
                        messagingViewModel.send(getMessagingAction()) { navigateUpToParent() }
                    }
                }
            }
        }
    }

    private fun getMessagingAction() = MessagingManager.AppUpdate(
        versionName = binding.editVersionName.text.toString(),
        versionCode = binding.editVersionCode.text.toString().toLong()
    )
}