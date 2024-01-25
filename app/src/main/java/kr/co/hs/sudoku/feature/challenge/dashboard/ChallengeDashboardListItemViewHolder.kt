package kr.co.hs.sudoku.feature.challenge.dashboard

import android.view.View
import androidx.core.view.isVisible
import coil.request.Disposable
import com.google.firebase.auth.FirebaseAuth
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.ViewHolder
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeButtonBinding
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeHeaderBinding
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeMatrixBinding
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeMyRankBinding
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeRankBinding
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeTitleBinding
import kr.co.hs.sudoku.extension.CoilExt.loadProfileImage
import kr.co.hs.sudoku.extension.NumberExtension.toTimerFormat
import java.text.SimpleDateFormat
import java.util.Locale

sealed class ChallengeDashboardListItemViewHolder<T : ChallengeDashboardListItem>(
    itemView: View
) : ViewHolder(itemView) {
    abstract val clickableView: View
    abstract fun onBind(item: T)
    fun setOnClickListener(l: View.OnClickListener) = clickableView.setOnClickListener(l)

    open fun onRecycled() {}

    class Title(val binding: LayoutListItemChallengeTitleBinding) :
        ChallengeDashboardListItemViewHolder<ChallengeDashboardListItem.TitleItem>(binding.root) {
        override val clickableView: View by lazy { binding.tvTitle }
        override fun onBind(item: ChallengeDashboardListItem.TitleItem) {
            with(binding.tvTitle) {
                text = getString(R.string.title_challenge)
            }
            with(binding.btnSelectDate) {
                visibility = if (item.date != null) View.VISIBLE else View.INVISIBLE
                text = itemView.context.getString(
                    R.string.challenge_select_date_format,
                    item.date?.let {
                        SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                        ).format(it)
                    }
                )
            }
        }
    }

    class MatrixInfo(private val binding: LayoutListItemChallengeMatrixBinding) :
        ChallengeDashboardListItemViewHolder<ChallengeDashboardListItem.MatrixItem>(binding.root) {
        override val clickableView: View by lazy { binding.matrix }
        override fun onBind(item: ChallengeDashboardListItem.MatrixItem) = with(binding.matrix) {
            matrix = item.matrix
            invalidate()
        }
    }

    class Rank(private val binding: LayoutListItemChallengeRankBinding) :
        ChallengeDashboardListItemViewHolder<ChallengeDashboardListItem.RankItem>(binding.root) {
        override val clickableView: View by lazy { binding.cardViewProfile }

        private var disposableIcon: Disposable? = null

        private val currentUserUid: String?
            get() = FirebaseAuth.getInstance().currentUser?.uid

        override fun onBind(item: ChallengeDashboardListItem.RankItem) {
            with(binding) {

                item.rankEntity.let { rankEntity ->
                    tvRank.text = when (val rank = rankEntity.rank) {
                        1L -> itemView.context.getString(R.string.rank_format_first)
                        2L -> itemView.context.getString(R.string.rank_format_second)
                        3L -> itemView.context.getString(R.string.rank_format_third)
                        else -> itemView.context.getString(R.string.rank_format, rank)
                    }
                    with(tvRecord) {
                        text = rankEntity.clearTime
                            .takeIf { it >= 0 }
                            ?.run { toTimerFormat() }
                            ?: 0L.toTimerFormat()
                        setTextColor(
                            if (currentUserUid == rankEntity.uid) {
                                getColorCompat(R.color.black)
                            } else {
                                getColorCompat(R.color.gray_600)
                            }
                        )
                    }

                    disposableIcon = ivProfileIcon.loadProfileImage(
                        rankEntity.iconUrl,
                        R.drawable.ic_person
                    )

                    with(tvDisplayName) {
                        text = rankEntity.displayName
                        setTextColor(
                            if (currentUserUid == rankEntity.uid) {
                                getColorCompat(R.color.black)
                            } else {
                                getColorCompat(R.color.gray_600)
                            }
                        )
                    }

                    with(cardViewProfile) {
                        isVisible = rankEntity.clearTime > 0
                    }

                    tvFlag.text = rankEntity.locale?.getLocaleFlag() ?: ""
                }


            }
        }

        override fun onRecycled() {
            super.onRecycled()
            disposableIcon?.dispose()
        }
    }

    class MyRank(private val binding: LayoutListItemChallengeMyRankBinding) :
        ChallengeDashboardListItemViewHolder<ChallengeDashboardListItem.MyRankItem>(binding.root) {
        override val clickableView: View by lazy { binding.root }

        private var disposableIcon: Disposable? = null
        override fun onBind(item: ChallengeDashboardListItem.MyRankItem) {
            with(binding) {
                item.rankEntity.let { rankEntity ->
                    tvRank.text = when (val rank = rankEntity.rank) {
                        1L -> itemView.context.getString(R.string.rank_format_first)
                        2L -> itemView.context.getString(R.string.rank_format_second)
                        3L -> itemView.context.getString(R.string.rank_format_third)
                        else -> itemView.context.getString(R.string.rank_format, rank)
                    }
                    with(tvRecord) {
                        text = rankEntity.clearTime.takeIf { it >= 0 }
                            ?.run { toTimerFormat() }
                            ?: 0L.toTimerFormat()
                        setTextColor(getColorCompat(R.color.black))
                    }

                    disposableIcon = ivProfileIcon.loadProfileImage(
                        rankEntity.iconUrl,
                        R.drawable.ic_person
                    )

                    with(tvDisplayName) {
                        text = rankEntity.displayName
                        setTextColor(getColorCompat(R.color.black))
                    }

                    with(cardViewProfile) {
                        isVisible = rankEntity.clearTime > 0
                    }

                    tvFlag.text = rankEntity.locale?.getLocaleFlag() ?: ""
                }
            }
        }

        override fun onRecycled() {
            super.onRecycled()
            disposableIcon?.dispose()
        }
    }

    class Button(private val binding: LayoutListItemChallengeButtonBinding) :
        ChallengeDashboardListItemViewHolder<ChallengeDashboardListItem.ChallengeStartItem>(binding.root) {
        override val clickableView: View by lazy { binding.cardView }
        override fun onBind(item: ChallengeDashboardListItem.ChallengeStartItem) {}
    }

    class MatrixHeader(private val binding: LayoutListItemChallengeHeaderBinding) :
        ChallengeDashboardListItemViewHolder<ChallengeDashboardListItem.MatrixHeaderItem>(binding.root) {
        override val clickableView: View by lazy { binding.tvHeader }
        override fun onBind(item: ChallengeDashboardListItem.MatrixHeaderItem) =
            with(binding.tvHeader) {
                text = getString(R.string.challenge_dashboard_header_matrix)
            }
    }

    class RankHeader(private val binding: LayoutListItemChallengeHeaderBinding) :
        ChallengeDashboardListItemViewHolder<ChallengeDashboardListItem.RankHeaderItem>(binding.root) {
        override val clickableView: View by lazy { binding.tvHeader }
        override fun onBind(item: ChallengeDashboardListItem.RankHeaderItem) =
            with(binding.tvHeader) {
                text = getString(R.string.challenge_dashboard_header_rank)
            }
    }
}