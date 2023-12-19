package kr.co.hs.sudoku.feature.matrixlist

import androidx.recyclerview.widget.DiffUtil

class MatrixListItemDiffCallback : DiffUtil.ItemCallback<MatrixListItem>() {
    override fun areItemsTheSame(oldItem: MatrixListItem, newItem: MatrixListItem) =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: MatrixListItem, newItem: MatrixListItem) =
        oldItem == newItem
}