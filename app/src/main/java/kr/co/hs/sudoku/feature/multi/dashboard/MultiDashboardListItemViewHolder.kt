package kr.co.hs.sudoku.feature.multi.dashboard

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import coil.request.Disposable
import kotlinx.coroutines.Job
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.ViewHolder
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayAdBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayAddFunctionBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayFilterBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayHeaderBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayTitleBinding
import kr.co.hs.sudoku.extension.CoilExt.loadProfileImage
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.battle.BattleLeaderBoardEntity
import kr.co.hs.sudoku.viewmodel.ViewModel

sealed class MultiDashboardListItemViewHolder<T : MultiDashboardListItem>(
    itemView: View
) : ViewHolder(itemView) {

    abstract val clickableView: View
    abstract fun onBind(item: T)
    fun setOnClickListener(l: View.OnClickListener) = clickableView.setOnClickListener(l)
    open fun onViewAttachedToWindow() {}
    open fun onViewDetachedFromWindow() {}

    class MultiPlay(
        val binding: LayoutListItemMultiPlayBinding,
    ) : MultiDashboardListItemViewHolder<MultiDashboardListItem.MultiPlayItem>(binding.root) {

        private var battleEntity: BattleEntity? = null

        override val clickableView: View by lazy { binding.cardView }
        override fun onBind(item: MultiDashboardListItem.MultiPlayItem) {
            with(binding.matrix) {
                setFixedCellValues(item.battleEntity.startingMatrix)
                invalidate()
            }

            battleEntity = item.battleEntity
        }

        private var requestProfileJob: Job? = null
        private var requestHostGradeJob: Job? = null
        private var requestGuestGradeJob: Job? = null
        private var disposableHostIcon: Disposable? = null
        private var disposableGuestIcon: Disposable? = null

        private val onResultParticipant: (ViewModel.RequestStatus<BattleEntity>) -> Unit = {
            when (it) {
                is ViewModel.OnStart -> with(binding) {
                    val ctx = itemView.context
                    ivHostIcon.isVisible = false
                    ivGuestIcon.isVisible = false
                    tvHostName.text = ctx.getString(R.string.loading_participants)
                    tvGuestName.text = ctx.getString(R.string.loading_participants)
                    tvHostGrade.isVisible = false
                    tvGuestGrade.isVisible = false
                }

                is ViewModel.OnError -> with(binding) {
                    val ctx = itemView.context
                    tvHostName.text = ctx.getString(R.string.error_participants)
                    tvGuestName.text = ctx.getString(R.string.error_participants)
                    ivHostIcon.isVisible = false
                    ivGuestIcon.isVisible = false
                }

                is ViewModel.OnFinish -> with(binding) {
                    with(it.d) {
                        val hostEntity = participants.find { it.uid == host }
                        val guestEntity = participants.filter { it.uid != host }.firstOrNull()

                        val viewModel = multiDashboardViewModel
                        hostEntity
                            ?.run {
                                disposableHostIcon =
                                    ivHostIcon.loadProfileImage(iconUrl, R.drawable.ic_person)
                                ivHostIcon.isVisible = true
                                tvHostName.text = displayName
                                requestHostGradeJob =
                                    viewModel?.requestStatistics(this, onResultHostGrade)
                            }
                            ?: run {
                                ivHostIcon.setImageDrawable(null)
                                ivHostIcon.isVisible = false
                                tvHostName.setText(R.string.empty_guest)
                            }

                        guestEntity
                            ?.run {
                                disposableGuestIcon =
                                    ivGuestIcon.loadProfileImage(iconUrl, R.drawable.ic_person)
                                ivGuestIcon.isVisible = true
                                tvGuestName.text = displayName
                                requestGuestGradeJob =
                                    viewModel?.requestStatistics(this, onResultGuestGrade)
                            }
                            ?: run {
                                ivGuestIcon.setImageDrawable(null)
                                ivGuestIcon.isVisible = false
                                tvGuestName.setText(R.string.empty_guest)
                            }
                    }
                }
            }

        }

        private val onResultHostGrade: (ViewModel.RequestStatus<BattleLeaderBoardEntity>) -> Unit =
            {
                when (it) {
                    is ViewModel.OnStart -> with(binding.tvHostGrade) {
                        text = context.getString(R.string.loading_statistics)
                        isVisible = false
                    }

                    is ViewModel.OnError -> with(binding.tvHostGrade) {
                        text = context.getString(R.string.error_statistics)
                        isVisible = false
                    }

                    is ViewModel.OnFinish -> with(binding.tvHostGrade) {
                        text = context.getString(
                            R.string.format_statistics_with_ranking,
                            it.d.winCount,
                            it.d.playCount - it.d.winCount,
                            when (it.d.ranking) {
                                0L -> context.getString(R.string.rank_format_nan)
                                1L -> context.getString(R.string.rank_format_first)
                                2L -> context.getString(R.string.rank_format_second)
                                3L -> context.getString(R.string.rank_format_third)
                                else -> context.getString(R.string.rank_format, it.d.ranking)
                            }
                        )
                        isVisible = true
                    }
                }
            }

        private val onResultGuestGrade: (ViewModel.RequestStatus<BattleLeaderBoardEntity>) -> Unit =
            {
                when (it) {
                    is ViewModel.OnStart -> with(binding.tvGuestGrade) {
                        text = context.getString(R.string.loading_statistics)
                        isVisible = false
                    }

                    is ViewModel.OnError -> with(binding.tvGuestGrade) {
                        text = context.getString(R.string.error_statistics)
                        isVisible = false
                    }

                    is ViewModel.OnFinish -> with(binding.tvGuestGrade) {
                        text = context.getString(
                            R.string.format_statistics_with_ranking,
                            it.d.winCount,
                            it.d.playCount - it.d.winCount,
                            when (it.d.ranking) {
                                0L -> context.getString(R.string.rank_format_nan)
                                1L -> context.getString(R.string.rank_format_first)
                                2L -> context.getString(R.string.rank_format_second)
                                3L -> context.getString(R.string.rank_format_third)
                                else -> context.getString(R.string.rank_format, it.d.ranking)
                            }
                        )
                        isVisible = true
                    }
                }

            }

        fun onRecycled() {
            requestProfileJob?.cancel()
            requestHostGradeJob?.cancel()
            requestGuestGradeJob?.cancel()
            disposableHostIcon?.dispose()
            disposableGuestIcon?.dispose()
        }

        override fun onViewAttachedToWindow() = multiDashboardViewModel
            ?.let { vm -> battleEntity?.let { vm.requestParticipant(it, onResultParticipant) } }
            ?.let { job -> requestProfileJob = job }
            ?: Unit

        private val multiDashboardViewModel: MultiDashboardViewModel?
            get() = itemView.findViewTreeViewModelStoreOwner()
                ?.let { vmStoreOwner -> ViewModelProvider(vmStoreOwner)[MultiDashboardViewModel::class.java] }
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

    class Filter(val binding: LayoutListItemMultiPlayFilterBinding) :
        MultiDashboardListItemViewHolder<MultiDashboardListItem.FilterItem>(binding.root) {
        override val clickableView: View by lazy { binding.checkboxShowOnlyEmpty }

        override fun onBind(item: MultiDashboardListItem.FilterItem) {
            binding.checkboxShowOnlyEmpty.isChecked = item.isOnlyEmpty
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