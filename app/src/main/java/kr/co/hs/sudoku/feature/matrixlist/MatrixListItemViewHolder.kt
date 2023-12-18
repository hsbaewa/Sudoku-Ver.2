package kr.co.hs.sudoku.feature.matrixlist

import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.view.updateLayoutParams
import kr.co.hs.sudoku.core.ViewHolder
import kr.co.hs.sudoku.databinding.LayoutMatrixListItemBinding
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.views.MatrixItemView

sealed class MatrixListItemViewHolder(val binding: LayoutMatrixListItemBinding) :
    ViewHolder(binding.root) {

    private lateinit var matrixItemView: MatrixItemView

    fun onBind(item: IntMatrix) {
        if (!this::matrixItemView.isInitialized) {
            with(binding.cardView) {
                matrixItemView = createMatrixItemView().also {
                    addView(it)
                    onMeasuredSize { w, h ->
                        it.updateLayoutParams {
                            width = w.minus(contentPaddingLeft.plus(contentPaddingRight))
                        }
                    }
                }

            }
        }

        matrixItemView.setMatrix(item)
    }

    private inline fun View.onMeasuredSize(crossinline onSize: (Int, Int) -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                onSize(measuredWidth, measuredHeight)
            }
        })
    }

    protected abstract fun createMatrixItemView(): MatrixItemView

    class BeginnerItemViewHolder(binding: LayoutMatrixListItemBinding) :
        MatrixListItemViewHolder(binding) {
        override fun createMatrixItemView() =
            MatrixItemView.BeginnerMatrixItemView(itemView.context)
    }

    class IntermediateItemViewHolder(binding: LayoutMatrixListItemBinding) :
        MatrixListItemViewHolder(binding) {
        override fun createMatrixItemView() =
            MatrixItemView.IntermediateMatrixItemView(itemView.context)
    }

    class AdvancedItemViewHolder(binding: LayoutMatrixListItemBinding) :
        MatrixListItemViewHolder(binding) {
        override fun createMatrixItemView() =
            MatrixItemView.AdvancedMatrixItemView(itemView.context)
    }
}