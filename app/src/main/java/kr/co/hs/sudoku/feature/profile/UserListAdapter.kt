package kr.co.hs.sudoku.feature.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import kr.co.hs.sudoku.databinding.LayoutListItemUserBinding
import kr.co.hs.sudoku.databinding.LayoutListItemUserHeaderBinding
import kr.co.hs.sudoku.databinding.LayoutListItemUserLabelEmptyBinding

class UserListAdapter :
    ListAdapter<UserListItem, UserListItemViewHolder<*>>(UserListDiffItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListItemViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            1 -> UserListHeaderItemViewHolder(
                LayoutListItemUserHeaderBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            2 -> UserListProfileItemViewHolder(
                LayoutListItemUserBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            3 -> UserListEmptyItemViewHolder(
                LayoutListItemUserLabelEmptyBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            else -> throw Exception("invalid view type")
        }
    }

    override fun onBindViewHolder(holder: UserListItemViewHolder<*>, position: Int) {
        when (holder) {
            is UserListHeaderItemViewHolder -> (getItem(position) as? UserListItem.Header)
                ?.run { holder.onBind(this) }

            is UserListProfileItemViewHolder -> (getItem(position) as? UserListItem.User)
                ?.run { holder.onBind(this) }
        }

    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is UserListItem.Header -> 1
        is UserListItem.User -> 2
        is UserListItem.EmptyMessage -> 3
    }

    override fun onViewRecycled(holder: UserListItemViewHolder<*>) {
        super.onViewRecycled(holder)
        when (holder) {
            is UserListProfileItemViewHolder -> holder.onRecycled()
        }
    }
}