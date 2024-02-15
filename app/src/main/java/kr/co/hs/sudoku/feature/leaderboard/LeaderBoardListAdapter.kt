package kr.co.hs.sudoku.feature.leaderboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import kr.co.hs.sudoku.databinding.LayoutListItemLeaderboardBinding
import kr.co.hs.sudoku.feature.profile.ProfilePopupMenu
import kr.co.hs.sudoku.repository.user.ProfileRepository

class LeaderBoardListAdapter(
    private val profileRepository: ProfileRepository,
    private val onPopupMenuItemClickListener: ProfilePopupMenu.OnPopupMenuItemClickListener
) : ListAdapter<LeaderBoardListItem, LeaderBoardListItemViewHolder<LeaderBoardListItem>>(
    LeaderBoardListItemDiffCallback()
) {

    companion object {
        private const val VT_BATTLE_ITEM = 100
        private const val VT_BATTLE_ITEM_FOR_MINE = 110
        private const val VT_CHALLENGE_ITEM = 200
        private const val VT_CHALLENGE_ITEM_FOR_MINE = 210
        private const val VT_EMPTY = 0
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LeaderBoardListItemViewHolder<LeaderBoardListItem> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutListItemLeaderboardBinding.inflate(inflater, parent, false)
        return when (viewType) {
            VT_BATTLE_ITEM -> BattleListItemViewHolder(binding, profileRepository).apply {
                binding.cardViewProfile.setOnClickListener {
                    it.showPopup(getItem(bindingAdapterPosition))
                }
            }

            VT_CHALLENGE_ITEM -> ChallengeListItemViewHolder(binding).apply {
                binding.cardViewProfile.setOnClickListener {
                    it.showPopup(getItem(bindingAdapterPosition))
                }
            }

            VT_BATTLE_ITEM_FOR_MINE -> BattleListItemViewHolder(binding, profileRepository)
            VT_CHALLENGE_ITEM_FOR_MINE -> ChallengeListItemViewHolder(binding)
            VT_EMPTY -> ChallengeListItemViewHolder(binding)
            else -> throw Exception("unknown view type")
        }
    }

    override fun onViewAttachedToWindow(holder: LeaderBoardListItemViewHolder<LeaderBoardListItem>) {
        holder.onViewAttachedToWindow()
    }

    override fun onViewDetachedFromWindow(holder: LeaderBoardListItemViewHolder<LeaderBoardListItem>) {
        holder.onViewDetachedFromWindow()
    }

    override fun onBindViewHolder(
        holder: LeaderBoardListItemViewHolder<LeaderBoardListItem>,
        position: Int
    ) {
        holder.onBind(getItem(position))
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is LeaderBoardListItem.BattleItem -> VT_BATTLE_ITEM
        is LeaderBoardListItem.ChallengeItem -> VT_CHALLENGE_ITEM
        is LeaderBoardListItem.BattleItemForMine -> VT_BATTLE_ITEM_FOR_MINE
        is LeaderBoardListItem.ChallengeItemForMine -> VT_CHALLENGE_ITEM_FOR_MINE
        is LeaderBoardListItem.Empty -> VT_EMPTY
    }

    override fun onViewRecycled(holder: LeaderBoardListItemViewHolder<LeaderBoardListItem>) {
        holder.onViewRecycled()
    }

    private fun View.showPopup(item: LeaderBoardListItem) {
        if (item.isMine)
            return

        ProfilePopupMenu(context, this, onPopupMenuItemClickListener)
            .show(item.id, item.displayName)
    }
}