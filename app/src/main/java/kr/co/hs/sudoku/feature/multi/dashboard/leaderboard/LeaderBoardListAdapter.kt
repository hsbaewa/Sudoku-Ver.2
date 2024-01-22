package kr.co.hs.sudoku.feature.multi.dashboard.leaderboard

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener
import androidx.recyclerview.widget.ListAdapter
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutListItemMultiLeaderboardMyRankBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMultiLeaderboardRankBinding
import kr.co.hs.sudoku.feature.profile.UserProfileViewModel

class LeaderBoardListAdapter(
    private val profileViewModel: UserProfileViewModel,
    private val onClickShowProfile: (uid: String) -> Boolean
) : ListAdapter<LeaderBoardItem, LeaderBoardItemViewHolder<out LeaderBoardItem>>(
    LeaderBoardItemDiffCallback()
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = when (viewType) {
        1 -> LeaderBoardListItemViewHolder(
            LayoutListItemMultiLeaderboardRankBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            profileViewModel
        ).apply {
            binding.cardViewProfile.setOnClickListener {
                val uid = getItem(bindingAdapterPosition).entity.uid
                ProfilePopupMenu(it.context, it).show(uid, onClickShowProfile)
            }
        }

        2 -> LeaderBoardMyItemViewHolder(
            LayoutListItemMultiLeaderboardMyRankBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), profileViewModel
        ).apply {
            binding.cardViewProfile.setOnClickListener {
                val uid = getItem(bindingAdapterPosition).entity.uid
                ProfilePopupMenu(it.context, it).show(uid, onClickShowProfile)
            }
        }

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

    private class ProfilePopupMenu(
        context: Context, anchor: View
    ) : PopupMenu(ContextThemeWrapper(context, R.style.Theme_HSSudoku2), anchor),
        OnMenuItemClickListener {
        var onClickShowProfile: ((String) -> Boolean)? = null
        var uid = ""

        fun show(uid: String, onClickShowProfile: (String) -> Boolean) {
            inflate(R.menu.profile)
            this.uid = uid
            this.onClickShowProfile = onClickShowProfile
            setOnMenuItemClickListener(this)
            show()
        }

        override fun onMenuItemClick(item: MenuItem?) = when (item?.itemId) {
            R.id.profile -> onClickShowProfile?.invoke(uid) ?: false
            else -> false
        }
    }

}