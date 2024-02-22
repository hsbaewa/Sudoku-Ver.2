package kr.co.hs.sudoku.feature.challenge.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeAdBinding
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeItemBinding
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeTitleBinding
import kr.co.hs.sudoku.feature.profile.ProfilePopupMenu
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository

class ChallengeDashboardListItemAdapter(
    private val repository: ChallengeRepository,
    private val onClickLeaderBoard: (challengeId: String) -> Unit,
    private val onClickStart: (ChallengeEntity) -> Unit,
    private val onPopupMenuItemClickListener: ProfilePopupMenu.OnPopupMenuItemClickListener
) : PagingDataAdapter<ChallengeDashboardListItem, ChallengeDashboardListItemViewHolder<ChallengeDashboardListItem>>(
    ChallengeDashboardListItemDiffCallback()
) {
    companion object {
        private const val VT_TITLE = 1
        private const val VT_ITEM = 2
        private const val VT_AD = 3
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChallengeDashboardListItemViewHolder<ChallengeDashboardListItem> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VT_TITLE -> {
                val binding = LayoutListItemChallengeTitleBinding.inflate(inflater, parent, false)
                ChallengeDashboardListItemTitleViewHolder(binding)
            }

            VT_ITEM -> {
                val binding = LayoutListItemChallengeItemBinding.inflate(inflater, parent, false)
                ChallengeDashboardListItemDefaultViewHolder<ChallengeDashboardListItem>(
                    binding,
                    repository,
                    onPopupMenuItemClickListener
                ).apply {
                    binding.challengeItemView.setOnClickShowLeaderBoard {
                        getItem(bindingAdapterPosition)?.id?.apply(onClickLeaderBoard)
                    }
                    binding.cardView.setOnClickListener {
                        (getItem(bindingAdapterPosition) as? ChallengeDashboardListItem.ChallengeItem)
                            ?.challengeEntity
                            ?.apply(onClickStart)
                    }
                }
            }

            VT_AD -> {
                val binding = LayoutListItemChallengeAdBinding.inflate(inflater, parent, false)
                AdItemViewHolder(binding)
            }

            else -> throw Exception("unknown view type")
        }
    }

    override fun onBindViewHolder(
        holder: ChallengeDashboardListItemViewHolder<ChallengeDashboardListItem>,
        position: Int
    ) {
        val item = getItem(position) ?: return
        when (holder) {
            is ChallengeDashboardListItemDefaultViewHolder -> holder.onBind(item)
            is ChallengeDashboardListItemTitleViewHolder -> holder.onBind(item)
            is AdItemViewHolder -> holder.onBind(item)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is ChallengeDashboardListItem.ChallengeItem -> VT_ITEM
        ChallengeDashboardListItem.TitleItem -> VT_TITLE
        is ChallengeDashboardListItem.AdItem -> VT_AD
        null -> throw Exception("unknown item")
    }

    override fun onViewAttachedToWindow(holder: ChallengeDashboardListItemViewHolder<ChallengeDashboardListItem>) {
        holder.onViewAttachedToWindow()
    }

    override fun onViewDetachedFromWindow(holder: ChallengeDashboardListItemViewHolder<ChallengeDashboardListItem>) {
        holder.onViewDetachedFromWindow()
    }

    override fun onViewRecycled(holder: ChallengeDashboardListItemViewHolder<ChallengeDashboardListItem>) {
        holder.onViewRecycled()
    }
}