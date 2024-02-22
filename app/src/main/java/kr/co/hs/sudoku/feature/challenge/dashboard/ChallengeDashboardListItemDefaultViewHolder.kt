package kr.co.hs.sudoku.feature.challenge.dashboard

import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeItemBinding
import kr.co.hs.sudoku.feature.profile.ProfilePopupMenu
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository

class ChallengeDashboardListItemDefaultViewHolder<T : ChallengeDashboardListItem>(
    val binding: LayoutListItemChallengeItemBinding,
    private val repository: ChallengeRepository,
    private val onPopupMenuItemClickListener: ProfilePopupMenu.OnPopupMenuItemClickListener
) : ChallengeDashboardListItemViewHolder<T>(binding.root) {

    private var challengeId: String? = null
    private var rankingLoadJob: Job? = null

    override fun onBind(item: ChallengeDashboardListItem) {
        challengeId = item.id

        when (item) {
            is ChallengeDashboardListItem.ChallengeItem -> onBindChallengeItem(item)
            else -> {}
        }
    }

    private fun onBindChallengeItem(item: ChallengeDashboardListItem.ChallengeItem) {
        binding.challengeItemView.set(item.challengeEntity)
    }

    override fun onViewAttachedToWindow() {
        rankingLoadJob = itemView.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            challengeId
                ?.let { id -> withContext(Dispatchers.IO) { repository.getRecords(id) } }
                ?.run { binding.challengeItemView.setLeaderBoard(this) }
        }
    }

    override fun onViewDetachedFromWindow() {}

    override fun onViewRecycled() {
        rankingLoadJob?.cancel()
        binding.challengeItemView.dispose()
    }
}