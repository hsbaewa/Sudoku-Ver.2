package kr.co.hs.sudoku.feature.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityManageChallengeCreateBinding
import kr.co.hs.sudoku.extension.platform.ActivityExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.isShowProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showProgressIndicator
import kr.co.hs.sudoku.feature.messaging.MessagingManager
import kr.co.hs.sudoku.model.challenge.ChallengeEntity

class ChallengeCreateActivity : Activity() {
    companion object {
        private fun newIntent(context: Context) =
            Intent(context, ChallengeCreateActivity::class.java)

        fun start(context: Context) = context.startActivity(newIntent(context))
    }

    private val binding: ActivityManageChallengeCreateBinding
            by lazy {
                DataBindingUtil.setContentView(this, R.layout.activity_manage_challenge_create)
            }
    private val viewModel: ChallengeManageViewModel by viewModels {
        val app = applicationContext as App
        ChallengeManageViewModel.ProviderFactory(app.getChallengeRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.error.observe(this) { it.showErrorAlert() }
        viewModel.isRunningProgress.observe(this) { isShowProgressIndicator = it }
        viewModel.generatedSudoku.observe(this) {
            binding.matrix.matrix = it
            binding.matrix.invalidate()
        }

        binding.btnGenerate.setOnClickListener { viewModel.generateChallengeSudoku() }
        binding.btnCreate.setOnClickListener {
            viewModel.createChallenge {
                lifecycleScope.launch {
                    showProgressIndicator()
                    if (it != null) {
                        withContext(Dispatchers.IO) { sendPush(it) }
                        navigateUpToParent()
                    }
                    dismissProgressIndicator()
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

    private suspend fun sendPush(challenge: ChallengeEntity?) = challenge?.createdAt
        ?.run { MessagingManager.NewChallenge(this) }
        ?.run {
            MessagingManager(applicationContext as App).sendNotification(this)
            true
        }
        ?: false
}