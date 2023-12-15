package kr.co.hs.sudoku.feature.matrixlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import kr.co.hs.sudoku.databinding.LayoutMatrixListItemBinding
import kr.co.hs.sudoku.feature.battle.SudokuMatrixItemDiffCallback
import kr.co.hs.sudoku.model.matrix.IntMatrix

class MatrixListItemAdapter :
    ListAdapter<IntMatrix, MatrixListItemViewHolder>(SudokuMatrixItemDiffCallback()) {

    companion object {
        private const val VT_BEGINNER = 100
        private const val VT_INTERMEDIATE = 200
        private const val VT_ADVANCED = 300
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatrixListItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutMatrixListItemBinding.inflate(inflater, parent, false)
        return when (viewType) {
            VT_BEGINNER -> MatrixListItemViewHolder.BeginnerItemViewHolder(binding)
            VT_INTERMEDIATE -> MatrixListItemViewHolder.IntermediateItemViewHolder(binding)
            VT_ADVANCED -> MatrixListItemViewHolder.AdvancedItemViewHolder(binding)
            else -> MatrixListItemViewHolder.BeginnerItemViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: MatrixListItemViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun getItemViewType(position: Int) = when (val item = getItem(position)) {
        is IntMatrix.Advanced -> VT_ADVANCED
        is IntMatrix.Beginner -> VT_BEGINNER
        is IntMatrix.Custom -> when (item.boxSize) {
            2 -> VT_BEGINNER
            3 -> VT_INTERMEDIATE
            4 -> VT_ADVANCED
            else -> throw Exception("존재 할수 없는 matrix")
        }

        is IntMatrix.Intermediate -> VT_INTERMEDIATE
    }
}