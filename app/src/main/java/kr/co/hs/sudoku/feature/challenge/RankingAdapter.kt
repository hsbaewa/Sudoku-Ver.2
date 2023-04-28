package kr.co.hs.sudoku.feature.challenge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import kr.co.hs.sudoku.databinding.LayoutItemRankBinding
import kr.co.hs.sudoku.model.rank.RankerEntity

class RankingAdapter(
    private val onItemClick: (position: Int, item: RankerEntity) -> Unit
) : ListAdapter<RankerEntity, RankViewHolder>(RankerEntityDiffCallback()) {

    companion object {
        const val VT_MINE = 10
        const val VT_OTHER = 11
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutItemRankBinding.inflate(inflater, parent, false)
        return when (viewType) {
            VT_MINE -> RankForMineViewHolder(binding).apply {
                itemView.setOnClickListener { itemClick(bindingAdapterPosition) }
            }

            else -> RankViewHolder(binding).apply {
                itemView.setOnClickListener { itemClick(bindingAdapterPosition) }
            }
        }
    }

    private val itemClick = { position: Int -> onItemClick(position, getItem(position)) }

    override fun onBindViewHolder(holder: RankViewHolder, position: Int) =
        holder.onBind(getItem(position))

    override fun onViewRecycled(holder: RankViewHolder) {
        super.onViewRecycled(holder)
        holder.onRecycled()
    }

    override fun getItemViewType(position: Int) =
        when (getItem(position).uid) {
            uid -> VT_MINE
            else -> VT_OTHER
        }

    var uid: String? = null
}