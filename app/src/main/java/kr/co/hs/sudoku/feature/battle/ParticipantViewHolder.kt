package kr.co.hs.sudoku.feature.battle

import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.ViewHolder
import kr.co.hs.sudoku.databinding.LayoutItemUserStatisticsBinding
import kr.co.hs.sudoku.model.battle.BattleStatisticsEntity
import kr.co.hs.sudoku.model.battle.ParticipantEntity
import kr.co.hs.sudoku.repository.battle.BattleRepository

class ParticipantViewHolder(
    private val binding: LayoutItemUserStatisticsBinding
) : ViewHolder(binding.root) {

    private val app: App by lazy { binding.root.context.applicationContext as App }
    private val battleRepository: BattleRepository by lazy { app.getBattleRepository2() }

    fun onBind(item: ParticipantEntity) {
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

        loadStatisticsJob = CoroutineScope(Dispatchers.Main.immediate).launch {
            val statistics =
                withContext(Dispatchers.IO) { battleRepository.getStatistics(item.uid) }
            onBind(statistics)
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

    private var loadStatisticsJob: Job? = null

    fun onRecycled() {
        binding.layoutUser.ivPhoto.dispose()
        binding.tvStatistics.visibility = View.GONE
        loadStatisticsJob?.cancel()
    }
}