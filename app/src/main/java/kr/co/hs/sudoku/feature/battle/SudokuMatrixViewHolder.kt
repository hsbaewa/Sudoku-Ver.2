package kr.co.hs.sudoku.feature.battle

import androidx.core.view.updateLayoutParams
import kr.co.hs.sudoku.core.ViewHolder
import kr.co.hs.sudoku.databinding.LayoutItemSudokuMatrixBinding
import kr.co.hs.sudoku.extension.NumberExtension.toPx
import kr.co.hs.sudoku.extension.platform.ViewExtension.observeMeasuredSize
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.views.SudokuBoardView

class SudokuMatrixViewHolder(
    private val binding: LayoutItemSudokuMatrixBinding,
    private val onItemClick: (Int, IntMatrix) -> Unit
) : ViewHolder(binding.root) {

    fun onBind(matrix: IntMatrix) {
        binding.cardView.setOnClickListener {
            onItemClick(bindingAdapterPosition, matrix)
        }

        binding.cardLayout.observeMeasuredSize { width, _ ->
            binding.sudokuBoard.setupUI(width, matrix)
        }
    }

    fun setChecked(checked: Boolean) {
        binding.cardView.isChecked = checked
    }

    private fun SudokuBoardView.setupUI(widthPx: Int, matrix: List<List<Int>>) {
        setRowCount(matrix.size, matrix)
        updateLayoutParams {
            this.width = widthPx - 20.toPx
            this.height = widthPx - 20.toPx
        }
    }
}