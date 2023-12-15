package kr.co.hs.sudoku.feature.matrixlist

import kr.co.hs.sudoku.core.ViewHolder
import kr.co.hs.sudoku.databinding.LayoutMatrixListItemBinding
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.views.MatrixItemView

sealed class MatrixListItemViewHolder(binding: LayoutMatrixListItemBinding) :
    ViewHolder(binding.root) {

    abstract val matrixItemView: MatrixItemView

    fun onBind(item: IntMatrix) = matrixItemView.setMatrix(item)

    class BeginnerItemViewHolder(binding: LayoutMatrixListItemBinding) :
        MatrixListItemViewHolder(binding) {
        override val matrixItemView: MatrixItemView by lazy {
            MatrixItemView.BeginnerMatrixItemView(itemView.context)
                .apply { binding.cardView.addView(this) }
        }
    }

    class IntermediateItemViewHolder(binding: LayoutMatrixListItemBinding) :
        MatrixListItemViewHolder(binding) {
        override val matrixItemView: MatrixItemView by lazy {
            MatrixItemView.IntermediateMatrixItemView(itemView.context)
                .apply { binding.cardView.addView(this) }
        }
    }

    class AdvancedItemViewHolder(binding: LayoutMatrixListItemBinding) :
        MatrixListItemViewHolder(binding) {
        override val matrixItemView: MatrixItemView by lazy {
            MatrixItemView.AdvancedMatrixItemView(itemView.context)
                .apply { binding.cardView.addView(this) }
        }
    }
}