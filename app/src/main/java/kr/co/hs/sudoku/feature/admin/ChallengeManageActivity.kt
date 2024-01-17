package kr.co.hs.sudoku.feature.admin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.core.ViewHolder
import kr.co.hs.sudoku.databinding.ActivityManageChallengeBinding
import kr.co.hs.sudoku.databinding.LayoutManagerChallengeListItemBinding
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.platform.ActivityExtension.isShowProgressIndicator
import kr.co.hs.sudoku.model.challenge.ChallengeEntity

class ChallengeManageActivity : Activity() {
    companion object {
        private fun newIntent(context: Context) =
            Intent(context, ChallengeManageActivity::class.java)

        fun start(context: Context) = context.startActivity(newIntent(context))
    }

    private val binding: ActivityManageChallengeBinding
            by lazy { DataBindingUtil.setContentView(this, R.layout.activity_manage_challenge) }
    private val challengeViewModel: ChallengeManageViewModel by viewModels {
        val app = applicationContext as App
        ChallengeManageViewModel.ProviderFactory(app.getChallengeRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        with(challengeViewModel) {
            error.observe(this@ChallengeManageActivity) { it.showErrorAlert() }
            isRunningProgress.observe(this@ChallengeManageActivity) {
                isShowProgressIndicator = it
            }
            challengeList.observe(this@ChallengeManageActivity) {
                (binding.recyclerViewChallengeList.adapter as ListItemAdapter).submitList(it)
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) { getChallengeList() }
            }
        }

        with(binding.recyclerViewChallengeList) {
            layoutManager = LinearLayoutManager(this@ChallengeManageActivity)
            addVerticalDivider(10.dp)
            adapter = ListItemAdapter { entity ->
                showConfirm(
                    R.string.admin_challenge_delete_alert_title,
                    R.string.admin_challenge_delete_alert_message
                ) {
                    if (it) {
                        challengeViewModel.deleteChallenge(entity)
                    }
                }
            }
        }

        binding.btnCreate.setOnClickListener { startCreateChallenge() }
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

    private class ItemViewHolder(private val binding: LayoutManagerChallengeListItemBinding) :
        ViewHolder(binding.root) {
        fun onBind(item: ChallengeEntity) {
            binding.tvChallengeId.text = item.challengeId
            binding.tvCreatedAt.text = item.createdAt?.toString()
        }
    }

    private class ItemDiffCallback : DiffUtil.ItemCallback<ChallengeEntity>() {
        override fun areItemsTheSame(oldItem: ChallengeEntity, newItem: ChallengeEntity) =
            oldItem.challengeId == newItem.challengeId

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: ChallengeEntity,
            newItem: ChallengeEntity
        ) = oldItem == newItem
    }


    private class ListItemAdapter(
        private val onItemClick: (ChallengeEntity) -> Unit
    ) : ListAdapter<ChallengeEntity, ItemViewHolder>(ItemDiffCallback()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = LayoutManagerChallengeListItemBinding.inflate(inflater, parent, false)
            return ItemViewHolder(binding).apply {
                binding.root.setOnClickListener { onItemClick(getItem(bindingAdapterPosition)) }
            }
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.onBind(getItem(position))
        }
    }

    private fun startCreateChallenge() =
        lifecycleScope.launch { ChallengeCreateActivity.start(this@ChallengeManageActivity) }
}