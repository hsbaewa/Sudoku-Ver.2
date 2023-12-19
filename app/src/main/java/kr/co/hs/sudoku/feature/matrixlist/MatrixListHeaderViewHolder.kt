package kr.co.hs.sudoku.feature.matrixlist

import kr.co.hs.sudoku.core.ViewHolder
import kr.co.hs.sudoku.databinding.LayoutMatrixListHeaderBinding

class MatrixListHeaderViewHolder(private val binding: LayoutMatrixListHeaderBinding) :
    ViewHolder(binding.root) {
    fun onBind(item: MatrixListItem.HeaderItem) = with(binding.tvHeader) {
        text = item.header
    }
}