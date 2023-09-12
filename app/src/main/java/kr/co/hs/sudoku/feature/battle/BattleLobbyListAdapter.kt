package kr.co.hs.sudoku.feature.battle

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import kr.co.hs.sudoku.databinding.LayoutItemBattleLobbyBinding
import kr.co.hs.sudoku.model.battle.BattleEntity

class BattleLobbyListAdapter(private val onItemClick: (BattleEntity) -> Unit) :
    ListAdapter<BattleEntity, BattleLobbyViewHolder>(BattleItemDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BattleLobbyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutItemBattleLobbyBinding.inflate(inflater, parent, false)
        return BattleLobbyViewHolder(binding).apply {
            binding.cardView.setOnClickListener { onItemClick(getItem(bindingAdapterPosition)) }
        }
    }

    override fun onBindViewHolder(holder: BattleLobbyViewHolder, position: Int) {
        val item = getItem(position)
        holder.onBind(item)
        holder.loadHost(item.host)
    }

    override fun onViewRecycled(holder: BattleLobbyViewHolder) {
        super.onViewRecycled(holder)
        holder.onRecycled()
    }
}