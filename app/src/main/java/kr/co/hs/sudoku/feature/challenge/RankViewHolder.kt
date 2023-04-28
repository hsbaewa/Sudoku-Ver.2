package kr.co.hs.sudoku.feature.challenge

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutItemRankBinding
import kr.co.hs.sudoku.extension.NumberExtension.toTimerFormat
import kr.co.hs.sudoku.model.rank.RankerEntity

open class RankViewHolder(val binding: LayoutItemRankBinding) : ViewHolder(binding.root) {
    fun onBind(rankerEntity: RankerEntity) {
        binding.tvRankOrder.text = rankerEntity.getFormattedRank()
        binding.tvFlag.text = rankerEntity.getLocaleFlag()
        binding.layoutUser.tvDisplayName.text = rankerEntity.getFormattedName()

        rankerEntity.iconUrl?.run {
            binding.layoutUser.ivPhoto.visibility = View.VISIBLE
            binding.layoutUser.ivPhoto.load(this)
        } ?: kotlin.run {
            binding.layoutUser.ivPhoto.visibility = View.GONE
        }

        binding.layoutUser.tvStatusMessage.run {
            rankerEntity.getFormattedMessage().takeIf { it != null }
                ?.let {
                    text = it
                    visibility = View.VISIBLE
                }
                ?: kotlin.run { visibility = View.GONE }
        }

        rankerEntity.clearTime.takeIf { it >= 0 }
            ?.run {
                binding.tvClearTime.visibility = View.VISIBLE
                binding.tvClearTime.text = rankerEntity.getFormattedClearTime()
            }
            ?: kotlin.run { binding.tvClearTime.visibility = View.GONE }
    }

    private fun RankerEntity.getFormattedRank() = when (rank) {
        1L -> itemView.context.getString(R.string.rank_format_first)
        2L -> itemView.context.getString(R.string.rank_format_second)
        3L -> itemView.context.getString(R.string.rank_format_third)
        else -> itemView.context.getString(R.string.rank_format, rank)
    }

    private fun RankerEntity.getLocaleFlag() = locale?.run { getLocaleFlag() }

    open fun RankerEntity.getFormattedName() = displayName
    private fun RankerEntity.getFormattedMessage() = message

    private fun RankerEntity.getFormattedClearTime() =
        clearTime.takeIf { it >= 0 }
            ?.run { itemView.context.getString(R.string.record_format, toTimerFormat()) }

    fun onRecycled() {
        binding.layoutUser.ivPhoto.dispose()
    }
}