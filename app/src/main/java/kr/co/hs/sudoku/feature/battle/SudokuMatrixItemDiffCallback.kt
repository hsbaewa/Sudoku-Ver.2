package kr.co.hs.sudoku.feature.battle

import androidx.recyclerview.widget.DiffUtil
import kr.co.hs.sudoku.model.matrix.IntMatrix

class SudokuMatrixItemDiffCallback : DiffUtil.ItemCallback<IntMatrix>() {
    override fun areItemsTheSame(oldItem: IntMatrix, newItem: IntMatrix): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: IntMatrix, newItem: IntMatrix): Boolean {
        return oldItem == newItem
    }
}