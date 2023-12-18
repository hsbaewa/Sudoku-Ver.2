package kr.co.hs.sudoku.feature.matrixlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import kr.co.hs.sudoku.databinding.LayoutMatrixListItemBinding
import kr.co.hs.sudoku.feature.battle.SudokuMatrixItemDiffCallback
import kr.co.hs.sudoku.model.matrix.IntMatrix

class MatrixListItemAdapter :
    ListAdapter<IntMatrix, MatrixListItemViewHolder>(SudokuMatrixItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatrixListItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutMatrixListItemBinding.inflate(inflater, parent, false)
        return MatrixListItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatrixListItemViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }
}