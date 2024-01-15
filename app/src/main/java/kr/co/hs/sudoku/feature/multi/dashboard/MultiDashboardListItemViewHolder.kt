package kr.co.hs.sudoku.feature.multi.dashboard

import android.view.View
import androidx.core.view.isVisible
import coil.load
import kotlinx.coroutines.Job
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.ViewHolder
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayAdBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayAddFunctionBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayHeaderBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayTitleBinding
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.battle.BattleStatisticsEntity
import kr.co.hs.sudoku.viewmodel.ViewModel

sealed class MultiDashboardListItemViewHolder<T : MultiDashboardListItem>(
    itemView: View
) : ViewHolder(itemView) {

    abstract val clickableView: View
    abstract fun onBind(item: T)
    fun setOnClickListener(l: View.OnClickListener) = clickableView.setOnClickListener(l)

    class MultiPlay(
        val binding: LayoutListItemMultiPlayBinding,
        private val viewModel: MultiDashboardViewModel
    ) : MultiDashboardListItemViewHolder<MultiDashboardListItem.MultiPlayItem>(binding.root) {
        override val clickableView: View by lazy { binding.cardView }
        override fun onBind(item: MultiDashboardListItem.MultiPlayItem) {
            with(binding.matrix) {
                matrix = item.battleEntity.startingMatrix
                invalidate()
            }

            requestParticipantJob =
                viewModel.requestParticipant(item.battleEntity, onResultParticipant)

        }

        private var requestParticipantJob: Job? = null
        private var requestStatisticsJob: Job? = null

        private val onResultParticipant: (ViewModel.RequestStatus<BattleEntity>) -> Unit = {
            when (it) {
                is ViewModel.OnStart -> with(binding) {
                    val ctx = itemView.context
                    tvDisplayName.text = ctx.getString(R.string.loading_participants)
                    tvMessage.isVisible = false
                }

                is ViewModel.OnError -> with(binding.tvDisplayName) {
                    text = context.getString(R.string.error_participants)
                }

                is ViewModel.OnFinish -> with(binding) {
                    with(it.d) {
                        participants
                            .find { participant -> participant.uid == host }
                            ?.let { owner ->
                                ivProfileIcon.load(owner.iconUrl) { crossfade(true) }
                                tvDisplayName.text = owner.displayName
                                owner.message
                                    ?.takeIf { it.isNotEmpty() }
                                    ?.run {
                                        tvMessage.text = this
                                        tvMessage.isVisible = true
                                    }
                                    ?: run { tvMessage.isVisible = false }

                                requestStatisticsJob =
                                    viewModel.requestStatistics(owner, onResultStatistics)
                            }
                    }
                }
            }

        }

        private val onResultStatistics: (ViewModel.RequestStatus<BattleStatisticsEntity>) -> Unit =
            {
                when (it) {
                    is ViewModel.OnStart -> with(binding.tvStatistics) {
                        text = context.getString(R.string.loading_statistics)
                    }

                    is ViewModel.OnError -> with(binding.tvStatistics) {
                        text = context.getString(R.string.error_statistics)
                    }

                    is ViewModel.OnFinish -> with(binding.tvStatistics) {
                        text = context.getString(
                            R.string.format_statistics,
                            it.d.playCount,
                            it.d.winCount
                        )
                    }
                }

            }

        fun onRecycled() {
            requestParticipantJob?.cancel()
            requestStatisticsJob?.cancel()
        }
    }

    class Title(val binding: LayoutListItemMultiPlayTitleBinding) :
        MultiDashboardListItemViewHolder<MultiDashboardListItem.TitleItem>(binding.root) {
        override val clickableView: View by lazy { binding.tvTitle }
        override fun onBind(item: MultiDashboardListItem.TitleItem) = with(binding.tvTitle) {
            text = context.getString(R.string.title_multi_play)
        }
    }

    class CreateNew(private val binding: LayoutListItemMultiPlayAddFunctionBinding) :
        MultiDashboardListItemViewHolder<MultiDashboardListItem.CreateNewItem>(binding.root) {
        override val clickableView: View by lazy { binding.cardView }
        override fun onBind(item: MultiDashboardListItem.CreateNewItem) {}
    }

    class HeaderUsers(private val binding: LayoutListItemMultiPlayHeaderBinding) :
        MultiDashboardListItemViewHolder<MultiDashboardListItem.HeaderUsersItem>(binding.root) {
        override val clickableView: View by lazy { binding.tvHeader }
        override fun onBind(item: MultiDashboardListItem.HeaderUsersItem) = with(binding.tvHeader) {
            text = context.getString(R.string.multi_play_header_users)
        }
    }

    class HeaderOthers(private val binding: LayoutListItemMultiPlayHeaderBinding) :
        MultiDashboardListItemViewHolder<MultiDashboardListItem.HeaderOthersItem>(binding.root) {
        override val clickableView: View by lazy { binding.tvHeader }
        override fun onBind(item: MultiDashboardListItem.HeaderOthersItem) =
            with(binding.tvHeader) {
                text = context.getString(R.string.multi_play_header_others)
            }
    }

    class AdItemView(private val binding: LayoutListItemMultiPlayAdBinding) :
        MultiDashboardListItemViewHolder<MultiDashboardListItem.AdItem>(binding.root) {
        override val clickableView: View by lazy { binding.root }
        override fun onBind(item: MultiDashboardListItem.AdItem) {
            with(binding.nativeAdView) {
                iconView = binding.ivIcon
                headlineView = binding.tvHeadline
                bodyView = binding.tvBody
                callToActionView = binding.cardView
                mediaView = binding.mediaView
            }

            item.nativeAd.mediaContent?.run {
                binding.mediaView.mediaContent = this
            }
            item.nativeAd.icon?.drawable?.run {
                binding.ivIcon.setImageDrawable(this)
            }
            item.nativeAd.headline?.run {
                binding.tvHeadline.text = this
            }
            item.nativeAd.body?.run {
                binding.tvBody.text = this
            }

            binding.nativeAdView.setNativeAd(item.nativeAd)
        }
    }
}