package kr.co.hs.sudoku.feature.matrixlist

import kr.co.hs.sudoku.core.ViewHolder
import kr.co.hs.sudoku.databinding.LayoutMatrixListItemBinding
import kr.co.hs.sudoku.model.matrix.IntMatrix

class MatrixListItemViewHolder(
    val binding: LayoutMatrixListItemBinding
) : ViewHolder(binding.root) {

    fun onBind(item: IntMatrix) = with(binding.matrix) {
        matrix = item
        invalidate()
    }
}