package kr.co.hs.sudoku.feature.battle

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import kr.co.hs.sudoku.databinding.LayoutItemSudokuMatrixBinding
import kr.co.hs.sudoku.model.matrix.IntMatrix

class SudokuMatrixListAdapter :
    ListAdapter<IntMatrix, SudokuMatrixViewHolder>(SudokuMatrixItemDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SudokuMatrixViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutItemSudokuMatrixBinding.inflate(inflater, parent, false)
        return SudokuMatrixViewHolder(binding) { position, _ ->
            selectedPosition.takeIf { it >= 0 }?.run { notifyItemChanged(this) }
            selectedPosition = position
            position.takeIf { it >= 0 }?.run { notifyItemChanged(this) }
        }
    }

    var selectedPosition = -1

    override fun onBindViewHolder(holder: SudokuMatrixViewHolder, position: Int) {
        holder.onBind(getItem(position))
        holder.setChecked(selectedPosition == position)
    }

    fun getSelectedItem() = selectedPosition
        .takeIf { it >= 0 }
        ?.run { getItem(this) }
}