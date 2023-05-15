package kr.co.hs.sudoku.feature.battle

import android.view.View
import androidx.core.view.updateLayoutParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.ViewHolder
import kr.co.hs.sudoku.databinding.LayoutItemBattleLobbyBinding
import kr.co.hs.sudoku.extension.NumberExtension.toPx
import kr.co.hs.sudoku.extension.platform.ViewExtension.observeMeasuredSize
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.views.SudokuBoardView

class BattleLobbyViewHolder(
    private val binding: LayoutItemBattleLobbyBinding,
    private val onItemClick: (Int, BattleEntity) -> Unit
) : ViewHolder(binding.root) {

    fun onBind(item: BattleEntity) {
        binding.cardView.setOnClickListener { onItemClick(bindingAdapterPosition, item) }
        binding.cardLayout.observeMeasuredSize { width, _ ->
            binding.sudokuBoard.setupUI(width, item.startingMatrix)
        }
        binding.layoutOwnerUser.tvStatusMessage.visibility = View.GONE
    }

    private fun SudokuBoardView.setupUI(widthPx: Int, matrix: List<List<Int>>) {
        setRowCount(matrix.size, matrix)
        updateLayoutParams {
            this.width = widthPx - 20.toPx
            this.height = widthPx - 20.toPx
        }
    }

    fun onRecycled() = loadProfileJob?.cancel()
    fun loadHost(uid: String) {
        loadProfileJob = CoroutineScope(Dispatchers.Main.immediate).launch {
            val profile =
                withContext(Dispatchers.IO) { app.getBattleRepository().getParticipant(uid) }

            binding.layoutOwnerUser.run {
                profile?.iconUrl?.run {
                    ivPhoto.load(this, errorIcon = getDrawableCompat(R.drawable.ic_person))
                    ivPhoto.visibility = View.VISIBLE
                } ?: kotlin.run {
                    ivPhoto.visibility = View.GONE
                }

                tvDisplayName.text = profile?.displayName
            }

        }
    }

    private var loadProfileJob: Job? = null
}