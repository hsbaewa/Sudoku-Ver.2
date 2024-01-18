package kr.co.hs.sudoku.feature.multi.dashboard.leaderboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import kr.co.hs.sudoku.databinding.LayoutListItemMultiLeaderboardMyRankBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMultiLeaderboardRankBinding
import kr.co.hs.sudoku.feature.UserProfileViewModel

class LeaderBoardListAdapter(
    private val profileViewModel: UserProfileViewModel
) : ListAdapter<LeaderBoardItem, LeaderBoardItemViewHolder<out LeaderBoardItem>>(
    LeaderBoardItemDiffCallback()
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = when (viewType) {
        1 -> LeaderBoardListItemViewHolder(
            LayoutListItemMultiLeaderboardRankBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            profileViewModel
        )

        2 -> LeaderBoardMyItemViewHolder(
            LayoutListItemMultiLeaderboardMyRankBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            profileViewModel
        )

        else -> throw Exception()
    }

    override fun onBindViewHolder(
        holder: LeaderBoardItemViewHolder<out LeaderBoardItem>,
        position: Int
    ) = when (val item = getItem(position)) {
        is LeaderBoardItem.ListItem -> (holder as LeaderBoardListItemViewHolder).onBind(item)
        is LeaderBoardItem.MyItem -> (holder as LeaderBoardMyItemViewHolder).onBind(item)
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is LeaderBoardItem.ListItem -> 1
        is LeaderBoardItem.MyItem -> 2
    }

    override fun onViewRecycled(holder: LeaderBoardItemViewHolder<out LeaderBoardItem>) {
        super.onViewRecycled(holder)
        holder.onRecycled()
    }
}