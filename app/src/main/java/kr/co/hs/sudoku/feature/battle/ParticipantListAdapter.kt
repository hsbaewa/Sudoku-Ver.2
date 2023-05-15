package kr.co.hs.sudoku.feature.battle

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import kr.co.hs.sudoku.databinding.LayoutItemUserStatisticsBinding
import kr.co.hs.sudoku.model.battle.BattleParticipantEntity

class ParticipantListAdapter :
    ListAdapter<BattleParticipantEntity, ParticipantViewHolder>(ParticipantItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutItemUserStatisticsBinding.inflate(inflater, parent, false)
        return ParticipantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        getItem(position)?.let { item ->
            holder.onBind(item)
            holder.loadStatistics(holder.app.getBattleRepository(), item.uid)
        } ?: kotlin.run {
            holder.onEmptyBind()
        }
    }

    override fun onViewRecycled(holder: ParticipantViewHolder) {
        super.onViewRecycled(holder)
        holder.onRecycled()
    }
}