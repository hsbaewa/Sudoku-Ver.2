package kr.co.hs.sudoku.feature.battle

import android.view.View
import androidx.core.view.updateLayoutParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.ViewHolder
import kr.co.hs.sudoku.databinding.LayoutItemBattleLobbyBinding
import kr.co.hs.sudoku.databinding.LayoutItemUserBinding
import kr.co.hs.sudoku.extension.NumberExtension.toPx
import kr.co.hs.sudoku.extension.platform.ViewExtension.observeMeasuredSize
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.repository.battle.BattleRepository
import kr.co.hs.sudoku.views.SudokuBoardView

class BattleLobbyViewHolder(private val binding: LayoutItemBattleLobbyBinding) :
    ViewHolder(binding.root) {

    private val app: App by lazy { binding.root.context.applicationContext as App }
    private val battleRepository: BattleRepository by lazy { app.getBattleRepository2() }

    fun onBind(item: BattleEntity) {
        with(binding) {
            cardLayout.observeMeasuredSize { width, _ ->
                binding.sudokuBoard.setupUI(width, item.startingMatrix)
            }
            layoutOwnerUser.loadHost(item)
        }


    }

    private fun SudokuBoardView.setupUI(widthPx: Int, matrix: List<List<Int>>) {
        setRowCount(matrix.size, matrix)
        updateLayoutParams {
            this.width = widthPx - 20.toPx
            this.height = widthPx - 20.toPx
        }
    }

    fun onRecycled() = loadProfileJob?.cancel()
    private fun LayoutItemUserBinding.loadHost(battleEntity: BattleEntity) {
        tvStatusMessage.visibility = View.GONE
        loadProfileJob = CoroutineScope(Dispatchers.Main.immediate).launch {
            val participants = battleEntity.participants.takeIf { it.isNotEmpty() }
                ?: run {
                    withContext(Dispatchers.IO) { battleRepository.getParticipants(battleEntity) }
                    battleEntity.participants
                }
            val profile = participants.find { it.uid == battleEntity.host }

            profile?.iconUrl?.run {
                ivPhoto.load(this, errorIcon = getDrawableCompat(R.drawable.ic_person))
                ivPhoto.visibility = View.VISIBLE
            } ?: kotlin.run {
                ivPhoto.visibility = View.GONE
            }

            tvDisplayName.text = profile?.displayName
        }
    }

    private var loadProfileJob: Job? = null
}