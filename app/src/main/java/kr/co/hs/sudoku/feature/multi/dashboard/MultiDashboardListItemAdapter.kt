package kr.co.hs.sudoku.feature.multi.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayAddFunctionBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMultiPlayTitleBinding
import kr.co.hs.sudoku.model.battle.BattleEntity

class MultiDashboardListItemAdapter(
    private val onItemClick: (BattleEntity) -> Unit,
    private val onCreateNew: () -> Unit
) : PagingDataAdapter<MultiDashboardListItem, MultiDashboardListItemViewHolder<out MultiDashboardListItem>>(
    MultiDashboardListItemDiffCallback()
) {
    companion object {
        const val VT_ITEM = 1020
        const val VT_TITLE = 1030
        const val VT_ADD = 1040
    }

    private lateinit var viewModel: MultiDashboardViewModel

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        viewModel =
            ViewModelProvider(recyclerView.context as ViewModelStoreOwner)[MultiDashboardViewModel::class.java]
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MultiDashboardListItemViewHolder<out MultiDashboardListItem> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VT_TITLE -> MultiDashboardListItemViewHolder.Title(
                LayoutListItemMultiPlayTitleBinding.inflate(inflater, parent, false)
            )

            VT_ITEM -> MultiDashboardListItemViewHolder.MultiPlay(
                LayoutListItemMultiPlayBinding.inflate(inflater, parent, false),
                viewModel
            ).apply {
                setOnClickListener {
                    (getItem(bindingAdapterPosition) as? MultiDashboardListItem.MultiPlayItem)
                        ?.battleEntity
                        ?.let(onItemClick)
                }
            }

            VT_ADD -> MultiDashboardListItemViewHolder.CreateNew(
                LayoutListItemMultiPlayAddFunctionBinding.inflate(inflater, parent, false)
            ).apply {
                setOnClickListener { onCreateNew() }
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

            null -> {}
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is MultiDashboardListItem.MultiPlayItem -> VT_ITEM
            is MultiDashboardListItem.TitleItem -> VT_TITLE
            MultiDashboardListItem.CreateNewItem -> VT_ADD
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
}