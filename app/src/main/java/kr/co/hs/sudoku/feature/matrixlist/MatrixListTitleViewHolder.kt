package kr.co.hs.sudoku.feature.matrixlist

import kr.co.hs.sudoku.core.ViewHolder
import kr.co.hs.sudoku.databinding.LayoutMatrixListTitleBinding

class MatrixListTitleViewHolder(private val binding: LayoutMatrixListTitleBinding) :
    ViewHolder(binding.root) {
    fun onBind(item: MatrixListItem.TitleItem) = with(binding.tvTitle) {
        text = item.title
    }
}