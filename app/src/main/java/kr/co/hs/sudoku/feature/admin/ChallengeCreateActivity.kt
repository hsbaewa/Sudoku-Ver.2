package kr.co.hs.sudoku.feature.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityManageChallengeCreateBinding
import kr.co.hs.sudoku.extension.platform.ActivityExtension.isShowProgressIndicator
import kr.co.hs.sudoku.feature.messaging.MessagingManager

@AndroidEntryPoint
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
    private val viewModel: ChallengeManageViewModel by viewModels()
    private val messagingViewModel: MessagingViewModel by viewModels()

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
                        setFixedCellValues(it)
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
            showConfirm(
                R.string.admin_challenge_create_alert_title,
                R.string.admin_challenge_create_alert_message
            ) {
                if (it) {
                    viewModel.createChallenge {
                        it?.run {
                            createdAt
                                ?.run { MessagingManager.NewChallenge(this) }
                                ?.run { messagingViewModel.send(this) { navigateUpToParent() } }
                        }
                    }
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