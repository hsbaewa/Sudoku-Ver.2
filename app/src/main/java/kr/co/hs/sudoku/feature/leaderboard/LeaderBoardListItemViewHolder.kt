package kr.co.hs.sudoku.feature.leaderboard

import android.view.View
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.ViewHolder
import kr.co.hs.sudoku.databinding.LayoutListItemLeaderboardBinding
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.views.ProfileView

abstract class LeaderBoardListItemViewHolder<out T : LeaderBoardListItem>(
    val binding: LayoutListItemLeaderboardBinding
) : ViewHolder(binding.root) {

    protected val tvDivider: TextView by lazy { binding.tvDivider }
    private val tvRank: TextView by lazy { binding.tvRank }
    private val tvRecord: TextView by lazy { binding.tvRecord }
    private val profileView: ProfileView by lazy { binding.profileView }
    protected val cardView: MaterialCardView by lazy { binding.cardViewProfile }

    abstract fun onBind(item: LeaderBoardListItem)
    abstract fun onViewAttachedToWindow()
    abstract fun onViewDetachedFromWindow()
    abstract fun onViewRecycled()


    protected fun setGrade(grade: Long) = with(tvRank) {
        when (grade) {
            1L -> {
                text = context.getString(R.string.rank_format_first)
                setTextColor(getColorCompat(R.color.gold))
            }

            2L -> {
                text = context.getString(R.string.rank_format_second)
                setTextColor(getColorCompat(R.color.silver))
            }

            3L -> {
                text = context.getString(R.string.rank_format_third)
                setTextColor(getColorCompat(R.color.bronze))
            }

            else -> {
                text = context.getString(R.string.rank_format, grade)
                setTextColor(getColorCompat(R.color.gray_600))
            }
        }
    }

    protected fun setProfile(profileEntity: ProfileEntity?, highlightName: Boolean = false) =
        with(profileView) {
            val disposable = profileEntity?.run {
                visibility = View.VISIBLE
                load(this)
            } ?: run {
                visibility = View.INVISIBLE
                null
            }

            setTextColor(
                if (highlightName) {
                    R.color.black
                } else {
                    R.color.gray_600
                }
            )

            return@with disposable
        }

    protected fun setRecord(record: String?) = with(tvRecord) {
        record
            ?.run { text = this }
            ?: run { text = "-" }
    }
}