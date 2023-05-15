package kr.co.hs.sudoku.feature.battle

import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.ViewHolder
import kr.co.hs.sudoku.databinding.LayoutItemUserStatisticsBinding
import kr.co.hs.sudoku.model.battle.BattleParticipantEntity
import kr.co.hs.sudoku.model.battle.BattleStatisticsEntity
import kr.co.hs.sudoku.repository.battle.BattleRepository

class ParticipantViewHolder(
    private val binding: LayoutItemUserStatisticsBinding
) : ViewHolder(binding.root) {

    fun onBind(item: BattleParticipantEntity) {
        binding.tvFlag.text = item.locale?.getLocaleFlag()
        binding.layoutUser.run {
            item.iconUrl?.run {
                ivPhoto.visibility = View.VISIBLE
                ivPhoto.load(this)
            } ?: kotlin.run {
                ivPhoto.visibility = View.GONE
            }
            tvDisplayName.text = item.displayName
            tvStatusMessage.text = item.message
        }
    }

    fun onEmptyBind() {
        binding.tvFlag.text = ""
        binding.layoutUser.run {
            ivPhoto.visibility = View.GONE
            tvDisplayName.text = "-"
            tvStatusMessage.visibility = View.GONE
        }
    }

    private fun onBind(statistics: BattleStatisticsEntity) {
        binding.tvStatistics.text = itemView.context.getString(
            R.string.format_statistics,
            statistics.clearedCount,
            statistics.winCount
        )
        binding.tvStatistics.visibility = View.VISIBLE
    }

    fun loadStatistics(battleRepository: BattleRepository, uid: String) {
        loadStatisticsJob = CoroutineScope(Dispatchers.Main).launch {
            val statistics = withContext(Dispatchers.IO) {
                battleRepository.getStatistics(uid)
            }
            onBind(statistics)
        }
    }

    private var loadStatisticsJob: Job? = null

    fun onRecycled() {
        binding.layoutUser.ivPhoto.dispose()
        binding.tvStatistics.visibility = View.GONE
        loadStatisticsJob?.cancel()
    }
}