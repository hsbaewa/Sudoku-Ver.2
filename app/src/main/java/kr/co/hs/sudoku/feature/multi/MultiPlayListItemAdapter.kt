package kr.co.hs.sudoku.feature.multi

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

class MultiPlayListItemAdapter(
    private val onItemClick: (BattleEntity) -> Unit,
    private val onCreateNew: () -> Unit
) : PagingDataAdapter<MultiPlayListItem, MultiPlayListItemViewHolder<out MultiPlayListItem>>(
    MultiPlayListItemDiffCallback()
) {
    companion object {
        const val VT_ITEM = 1020
        const val VT_TITLE = 1030
        const val VT_ADD = 1040
    }

    private lateinit var viewModel: MultiPlayListViewModel

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        viewModel =
            ViewModelProvider(recyclerView.context as ViewModelStoreOwner)[MultiPlayListViewModel::class.java]
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MultiPlayListItemViewHolder<out MultiPlayListItem> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VT_TITLE -> MultiPlayListItemViewHolder.Title(
                LayoutListItemMultiPlayTitleBinding.inflate(inflater, parent, false)
            )

            VT_ITEM -> MultiPlayListItemViewHolder.MultiPlay(
                LayoutListItemMultiPlayBinding.inflate(inflater, parent, false),
                viewModel
            ).apply {
                setOnClickListener {
                    (getItem(bindingAdapterPosition) as? MultiPlayListItem.MultiPlayItem)
                        ?.battleEntity
                        ?.let(onItemClick)
                }
            }

            VT_ADD -> MultiPlayListItemViewHolder.CreateNew(
                LayoutListItemMultiPlayAddFunctionBinding.inflate(inflater, parent, false)
            ).apply {
                setOnClickListener { onCreateNew() }
            }

            else -> throw Exception("invalid type")
        }
    }

    override fun onBindViewHolder(
        holder: MultiPlayListItemViewHolder<out MultiPlayListItem>,
        position: Int
    ) {
        when (val item = getItem(position)) {
            is MultiPlayListItem.MultiPlayItem ->
                (holder as MultiPlayListItemViewHolder.MultiPlay).onBind(item)

            is MultiPlayListItem.TitleItem ->
                (holder as MultiPlayListItemViewHolder.Title).onBind(item)

            is MultiPlayListItem.CreateNewItem ->
                (holder as MultiPlayListItemViewHolder.CreateNew).onBind(item)

            null -> {}
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is MultiPlayListItem.MultiPlayItem -> VT_ITEM
            is MultiPlayListItem.TitleItem -> VT_TITLE
            MultiPlayListItem.CreateNewItem -> VT_ADD
            null -> -1
        }
    }

    override fun onViewRecycled(holder: MultiPlayListItemViewHolder<out MultiPlayListItem>) {
        super.onViewRecycled(holder)
        when (holder) {
            is MultiPlayListItemViewHolder.MultiPlay -> holder.onRecycled()
            else -> {}
        }
    }
}