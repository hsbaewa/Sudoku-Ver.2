package kr.co.hs.sudoku.feature.leaderboard

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import coil.request.Disposable
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutListItemLeaderboardBinding
import kr.co.hs.sudoku.model.battle.BattleLeaderBoardEntity
import kr.co.hs.sudoku.repository.user.ProfileRepository

class BattleListItemViewHolder(
    binding: LayoutListItemLeaderboardBinding,
    private val profileRepository: ProfileRepository
) : LeaderBoardListItemViewHolder<LeaderBoardListItem.BattleItem>(binding) {

    private var requestProfileJob: Job? = null
    private var disposableProfileIcon: Disposable? = null

    private var entity: BattleLeaderBoardEntity? = null
    private var isMine: Boolean = false

    override fun onBind(item: LeaderBoardListItem) {
        when (item) {
            is LeaderBoardListItem.BattleItem -> {
                entity = item.entity
                isMine = item.isMine
                setGrade(item.grade)
                setRecord(
                    itemView.context.getString(
                        R.string.format_statistics,
                        item.entity.winCount,
                        item.entity.playCount - item.entity.winCount
                    )
                )
                tvDivider.isVisible = false
            }

            is LeaderBoardListItem.BattleItemForMine -> {
                entity = item.entity
                isMine = true
                setGrade(item.grade)
                setRecord(
                    itemView.context.getString(
                        R.string.format_statistics,
                        item.entity.winCount,
                        item.entity.playCount - item.entity.winCount
                    )
                )
                tvDivider.isVisible = true
            }

            is LeaderBoardListItem.Empty -> {
                entity = null
                isMine = false
                setGrade(item.grade)
                setRecord(null)
                setProfile(null)
                tvDivider.isVisible = false
            }

            else -> {}
        }
    }

    override fun onViewAttachedToWindow() {
        requestProfileJob = itemView.findViewTreeLifecycleOwner()?.lifecycleScope
            ?.launch(CoroutineExceptionHandler { _, _ -> cardView.visibility = View.INVISIBLE }) {
                val uid = entity?.uid ?: return@launch
                profileRepository
                    .runCatching {
                        disposableProfileIcon = getProfile(uid)
                            .run { this@BattleListItemViewHolder.setProfile(this, isMine) }
                        cardView.visibility = View.VISIBLE
                    }
                    .getOrElse {
                        cardView.visibility = View.INVISIBLE
                    }
            }
    }

    override fun onViewDetachedFromWindow() {}

    override fun onViewRecycled() {
        requestProfileJob?.cancel()
        disposableProfileIcon?.dispose()
    }
}