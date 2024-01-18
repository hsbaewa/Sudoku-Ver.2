package kr.co.hs.sudoku.feature.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityManageChallengeCreateBinding
import kr.co.hs.sudoku.extension.platform.ActivityExtension.isShowProgressIndicator
import kr.co.hs.sudoku.feature.messaging.MessagingManager

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
    private val app: App by lazy { applicationContext as App }
    private val viewModel: ChallengeManageViewModel by viewModels {
        ChallengeManageViewModel.ProviderFactory(app.getChallengeRepository())
    }
    private val messagingViewModel: MessagingViewModel by viewModels {
        val messagingManager = MessagingManager(app)
        MessagingViewModel.ProviderFactory(messagingManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel
            .let { vm ->
                vm.error.observe(this) { it.showErrorAlert() }
                vm.isRunningProgress.observe(this) { isShowProgressIndicator = it }
                vm.generatedSudoku.observe(this) {
                    with(binding.matrix) {
                        matrix = it
                        invalidate()
                    }
                }
            }

        messagingViewModel
            .let { vm ->
                vm.error.observe(this) { it.showErrorAlert() }
                vm.isRunningProgress.observe(this) { isShowProgressIndicator = it }
            }

        binding.btnGenerate.setOnClickListener { viewModel.generateChallengeSudoku() }
        binding.btnCreate.setOnClickListener {
            viewModel.createChallenge {
                it?.run {
                    createdAt
                        ?.run { MessagingManager.NewChallenge(this) }
                        ?.run { messagingViewModel.send(this) { navigateUpToParent() } }
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
}