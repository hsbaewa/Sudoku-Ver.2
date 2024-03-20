package kr.co.hs.sudoku.feature.leaderboard

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import coil.request.Disposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutListItemLeaderboardBinding
import kr.co.hs.sudoku.feature.user.Authenticator
import kr.co.hs.sudoku.model.battle.BattleLeaderBoardEntity

class BattleListItemViewHolder(
    binding: LayoutListItemLeaderboardBinding,
    private val authenticator: Authenticator
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
            ?.launch {
                val uid = entity?.uid ?: return@launch
                authenticator.getProfile(uid)
                    .catch { cardView.visibility = View.INVISIBLE }
                    .collect {
                        disposableProfileIcon = setProfile(it, isMine)
                        cardView.visibility = View.VISIBLE
                    }
            }
    }

    override fun onViewDetachedFromWindow() {}

    override fun onViewRecycled() {
        requestProfileJob?.cancel()
        disposableProfileIcon?.dispose()
    }
}