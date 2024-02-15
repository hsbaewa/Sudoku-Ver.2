package kr.co.hs.sudoku.feature.leaderboard

import android.view.View
import androidx.core.view.isVisible
import coil.request.Disposable
import kr.co.hs.sudoku.databinding.LayoutListItemLeaderboardBinding
import kr.co.hs.sudoku.extension.NumberExtension.toTimerFormat
import kr.co.hs.sudoku.model.rank.RankerEntity

class ChallengeListItemViewHolder(binding: LayoutListItemLeaderboardBinding) :
    LeaderBoardListItemViewHolder<LeaderBoardListItem.ChallengeItem>(binding) {

    private var entity: RankerEntity? = null
    private var isMine: Boolean = false
    private var disposableProfileIcon: Disposable? = null

    override fun onBind(item: LeaderBoardListItem) {

        when (item) {
            is LeaderBoardListItem.ChallengeItem -> {
                entity = item.entity
                isMine = item.isMine
                setGrade(item.grade)
                setRecord(item.entity.clearTime.toTimerFormat())
                tvDivider.isVisible = false
            }

            is LeaderBoardListItem.ChallengeItemForMine -> {
                entity = item.entity
                isMine = true
                setGrade(item.grade)
                setRecord(item.entity.clearTime.toTimerFormat())
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
        disposableProfileIcon = entity?.run {
            cardView.visibility = View.VISIBLE
            setProfile(this, isMine)
        } ?: run {
            cardView.visibility = View.INVISIBLE
            null
        }
    }

    override fun onViewDetachedFromWindow() {}
    override fun onViewRecycled() {
        disposableProfileIcon?.dispose()
    }
}