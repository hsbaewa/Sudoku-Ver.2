package kr.co.hs.sudoku.feature.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import kr.co.hs.sudoku.databinding.LayoutListItemUserBinding
import kr.co.hs.sudoku.databinding.LayoutListItemUserHeaderBinding
import kr.co.hs.sudoku.databinding.LayoutListItemUserLabelEmptyBinding

class OnlineUserListAdapter(
    private val onPopupMenuItemClickListener: ProfilePopupMenu.OnPopupMenuItemClickListener
) : ListAdapter<OnlineUserListItem, OnlineUserListItemViewHolder<*>>(OnlineUserListDiffItemCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OnlineUserListItemViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            1 -> OnlineUserListHeaderItemViewHolder(
                LayoutListItemUserHeaderBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            2 -> OnlineUserListProfileItemViewHolder(
                LayoutListItemUserBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            ).apply {
                binding.cardViewProfile.setOnClickListener {
                    with(ProfilePopupMenu(it.context, it, onPopupMenuItemClickListener)) {
                        (getItem(bindingAdapterPosition) as? OnlineUserListItem.User)
                            ?.profile
                            ?.run { show(this) }
                    }

                }
            }

            3 -> OnlineUserListEmptyItemViewHolder(
                LayoutListItemUserLabelEmptyBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            4 -> OnlineUserListMyProfileItemViewHolder(
                LayoutListItemUserBinding.inflate(inflater, parent, false)
            )

            else -> throw Exception("invalid view type")
        }
    }

    override fun onBindViewHolder(holder: OnlineUserListItemViewHolder<*>, position: Int) {
        when (holder) {
            is OnlineUserListHeaderItemViewHolder -> (getItem(position) as? OnlineUserListItem.Header)
                ?.run { holder.onBind(this) }

            is OnlineUserListProfileItemViewHolder -> (getItem(position) as? OnlineUserListItem.User)
                ?.run { holder.onBind(this) }

            is OnlineUserListMyProfileItemViewHolder -> (getItem(position) as? OnlineUserListItem.UserForMe)
                ?.run { holder.onBind(this) }
        }

    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is OnlineUserListItem.Header -> 1
        is OnlineUserListItem.User -> 2
        is OnlineUserListItem.EmptyMessage -> 3
        is OnlineUserListItem.UserForMe -> 4
    }

    override fun onViewRecycled(holder: OnlineUserListItemViewHolder<*>) {
        super.onViewRecycled(holder)
        when (holder) {
            is OnlineUserListProfileItemViewHolder -> holder.onRecycled()
            is OnlineUserListMyProfileItemViewHolder -> holder.onRecycled()
        }
    }
}