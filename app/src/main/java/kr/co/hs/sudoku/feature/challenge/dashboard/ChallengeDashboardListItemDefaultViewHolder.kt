package kr.co.hs.sudoku.feature.challenge.dashboard

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import coil.request.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeItemBinding
import kr.co.hs.sudoku.feature.profile.ProfilePopupMenu
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.views.ProfileView
import java.text.SimpleDateFormat
import java.util.Locale

class ChallengeDashboardListItemDefaultViewHolder<T : ChallengeDashboardListItem>(
    val binding: LayoutListItemChallengeItemBinding,
    private val repository: ChallengeRepository,
    private val onPopupMenuItemClickListener: ProfilePopupMenu.OnPopupMenuItemClickListener
) : ChallengeDashboardListItemViewHolder<T>(binding.root) {

    private var challengeId: String? = null
    private var rankingLoadJob: Job? = null
    private var disposableProfileFirst: Disposable? = null
    private var disposableProfileSecond: Disposable? = null
    private var disposableProfileThird: Disposable? = null

    override fun onBind(item: ChallengeDashboardListItem) {
        challengeId = item.id

        when (item) {
            is ChallengeDashboardListItem.ChallengeItem -> with(binding) {
                tvTitle.text = item.challengeEntity.createdAt
                    ?.run {
                        SimpleDateFormat(
                            itemView.context.getString(R.string.challenge_list_item_created_at_format),
                            Locale.getDefault()
                        ).format(this)
                    }
                    ?: itemView.context.getString(R.string.no_name)

                matrix.setFixedCellValues(item.challengeEntity.matrix)
                cardViewFirstGrade.setOnClickListener {
                    profileViewFirstGrade.currentProfile?.run { onClickProfile(it, this) }
                }
                cardViewSecondGrade.setOnClickListener {
                    profileViewSecondGrade.currentProfile?.run { onClickProfile(it, this) }
                }
                cardViewThirdGrade.setOnClickListener {
                    profileViewThirdGrade.currentProfile?.run { onClickProfile(it, this) }
                }
            }

            else -> {}
        }
    }

    override fun onViewAttachedToWindow() {
        rankingLoadJob = itemView.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            challengeId
                ?.let { id -> withContext(Dispatchers.IO) { repository.getRecords(id) } }
                ?.run { onLoadedLeaderboard(this) }
        }
    }

    private fun onLoadedLeaderboard(list: List<RankerEntity>) {
        list.getOrNull(0)
            ?.run {
                disposableProfileFirst = binding.profileViewFirstGrade.loadProfile(this)
                binding.cardViewFirstGrade.isVisible = true
            }
            ?: run { binding.cardViewFirstGrade.isVisible = false }

        list.getOrNull(1)
            ?.run {
                disposableProfileSecond = binding.profileViewSecondGrade.loadProfile(this)
                binding.cardViewSecondGrade.isVisible = true
            }
            ?: run { binding.cardViewSecondGrade.isVisible = false }

        list.getOrNull(2)
            ?.run {
                disposableProfileThird = binding.profileViewThirdGrade.loadProfile(this)
                binding.cardViewThirdGrade.isVisible = true
            }
            ?: run { binding.cardViewThirdGrade.isVisible = false }

        binding.btnLeaderBoard.isVisible = list.isNotEmpty()
        binding.tvDescription.isVisible = list.isEmpty()
    }

    private fun ProfileView.loadProfile(profileEntity: ProfileEntity?): Disposable? {
        return profileEntity
            ?.run { load(this) }
            ?: run {
                clear()
                null
            }
    }

    override fun onViewDetachedFromWindow() {}

    override fun onViewRecycled() {
        rankingLoadJob?.cancel()
        disposableProfileFirst?.dispose()
        disposableProfileSecond?.dispose()
        disposableProfileThird?.dispose()
    }

    private fun onClickProfile(parent: View, profileEntity: ProfileEntity) =
        ProfilePopupMenu(parent.context, parent, onPopupMenuItemClickListener).show(profileEntity)
}