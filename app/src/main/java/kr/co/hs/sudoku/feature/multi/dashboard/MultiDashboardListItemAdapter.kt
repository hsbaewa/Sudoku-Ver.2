package kr.co.hs.sudoku.feature.multi.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayAdBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayAddFunctionBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayFilterBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayHeaderBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayTitleBinding

class MultiDashboardListItemAdapter(
    private val onItemClick: (MultiDashboardListItem.MultiPlayItem) -> Unit,
    private val onCreateNew: () -> Unit,
    private val onClickLeaderBoard: () -> Unit,
    private val onCheckedOnlyEmpty: (Boolean) -> Unit
) : PagingDataAdapter<MultiDashboardListItem, MultiDashboardListItemViewHolder<out MultiDashboardListItem>>(
    MultiDashboardListItemDiffCallback()
) {
    companion object {
        const val VT_ITEM = 1020
        const val VT_TITLE = 1030
        const val VT_ADD = 1040
        const val VT_HEADER_USERS = 1050
        const val VT_HEADER_OTHERS = 1060
        const val VT_AD = 1070
        const val VT_FILTER = 1080
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MultiDashboardListItemViewHolder<out MultiDashboardListItem> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VT_TITLE -> MultiDashboardListItemViewHolder.Title(
                LayoutListItemMultiPlayTitleBinding.inflate(inflater, parent, false)
            ).apply {
                binding.btnLeaderBoard.setOnClickListener { onClickLeaderBoard() }
            }

            VT_ITEM -> MultiDashboardListItemViewHolder.MultiPlay(
                LayoutListItemMultiPlayBinding.inflate(inflater, parent, false)
            ).apply {
                setOnClickListener {
                    (getItem(bindingAdapterPosition) as? MultiDashboardListItem.MultiPlayItem)
                        ?.let(onItemClick)
                }
            }

            VT_ADD -> MultiDashboardListItemViewHolder.CreateNew(
                LayoutListItemMultiPlayAddFunctionBinding.inflate(inflater, parent, false)
            ).apply {
                setOnClickListener { onCreateNew() }
            }

            VT_HEADER_USERS -> MultiDashboardListItemViewHolder.HeaderUsers(
                LayoutListItemMultiPlayHeaderBinding.inflate(inflater, parent, false)
            )

            VT_HEADER_OTHERS -> MultiDashboardListItemViewHolder.HeaderOthers(
                LayoutListItemMultiPlayHeaderBinding.inflate(inflater, parent, false)
            )

            VT_AD -> MultiDashboardListItemViewHolder.AdItemView(
                LayoutListItemMultiPlayAdBinding.inflate(inflater, parent, false)
            )

            VT_FILTER -> MultiDashboardListItemViewHolder.Filter(
                LayoutListItemMultiPlayFilterBinding.inflate(inflater, parent, false)
            ).apply {
                setOnClickListener { onCheckedOnlyEmpty(binding.checkboxShowOnlyEmpty.isChecked) }
            }

            else -> throw Exception("invalid type")
        }
    }

    override fun onBindViewHolder(
        holder: MultiDashboardListItemViewHolder<out MultiDashboardListItem>,
        position: Int
    ) {
        when (val item = getItem(position)) {
            is MultiDashboardListItem.MultiPlayItem ->
                (holder as MultiDashboardListItemViewHolder.MultiPlay).onBind(item)

            is MultiDashboardListItem.TitleItem ->
                (holder as MultiDashboardListItemViewHolder.Title).onBind(item)

            is MultiDashboardListItem.CreateNewItem ->
                (holder as MultiDashboardListItemViewHolder.CreateNew).onBind(item)

            is MultiDashboardListItem.HeaderOthersItem ->
                (holder as MultiDashboardListItemViewHolder.HeaderOthers).onBind(item)

            is MultiDashboardListItem.HeaderUsersItem ->
                (holder as MultiDashboardListItemViewHolder.HeaderUsers).onBind(item)

            is MultiDashboardListItem.AdItem ->
                (holder as MultiDashboardListItemViewHolder.AdItemView).onBind(item)

            is MultiDashboardListItem.FilterItem ->
                (holder as MultiDashboardListItemViewHolder.Filter).onBind(item)

            null -> {}
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is MultiDashboardListItem.MultiPlayItem -> VT_ITEM
            is MultiDashboardListItem.TitleItem -> VT_TITLE
            MultiDashboardListItem.CreateNewItem -> VT_ADD
            MultiDashboardListItem.HeaderOthersItem -> VT_HEADER_OTHERS
            MultiDashboardListItem.HeaderUsersItem -> VT_HEADER_USERS
            is MultiDashboardListItem.AdItem -> VT_AD
            is MultiDashboardListItem.FilterItem -> VT_FILTER
            null -> -1
        }
    }

    override fun onViewRecycled(holder: MultiDashboardListItemViewHolder<out MultiDashboardListItem>) {
        super.onViewRecycled(holder)
        when (holder) {
            is MultiDashboardListItemViewHolder.MultiPlay -> holder.onRecycled()
            else -> {}
        }
    }

    override fun onViewAttachedToWindow(holder: MultiDashboardListItemViewHolder<out MultiDashboardListItem>) {
        super.onViewAttachedToWindow(holder)
        holder.onViewAttachedToWindow()
    }

    override fun onViewDetachedFromWindow(holder: MultiDashboardListItemViewHolder<out MultiDashboardListItem>) {
        super.onViewDetachedFromWindow(holder)
        holder.onViewDetachedFromWindow()
    }
}