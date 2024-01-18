package kr.co.hs.sudoku.feature.multi.dashboard.leaderboard

import android.view.View
import android.widget.ImageView
import coil.request.Disposable
import kotlinx.coroutines.Job
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.ViewHolder
import kr.co.hs.sudoku.extension.CoilExt.loadProfileImage
import kr.co.hs.sudoku.model.battle.BattleLeaderBoardEntity

abstract class LeaderBoardItemViewHolder<T : LeaderBoardItem>(itemView: View) :
    ViewHolder(itemView) {
    abstract fun onBind(item: T)
    fun onRecycled() {
        requestProfileJob?.cancel()
        disposableProfileIcon?.dispose()
    }

    protected fun getRankingText(ranking: Long) = when (ranking) {
        1L -> itemView.context.getString(R.string.rank_format_first)
        2L -> itemView.context.getString(R.string.rank_format_second)
        3L -> itemView.context.getString(R.string.rank_format_third)
        else -> itemView.context.getString(R.string.rank_format, ranking)
    }

    protected fun getStatisticsText(entity: BattleLeaderBoardEntity) =
        itemView.context.getString(
            R.string.format_statistics,
            entity.playCount,
            entity.winCount
        )

    protected var requestProfileJob: Job? = null
    protected fun ImageView.loadProfileImage(data: String?) =
        loadProfileImage(data, R.drawable.ic_person)
            .apply { disposableProfileIcon = this }

    protected var disposableProfileIcon: Disposable? = null
}