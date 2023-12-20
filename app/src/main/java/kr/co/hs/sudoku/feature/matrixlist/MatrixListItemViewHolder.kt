package kr.co.hs.sudoku.feature.matrixlist

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import kr.co.hs.sudoku.core.ViewHolder
import kr.co.hs.sudoku.databinding.LayoutListItemMatrixBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMatrixHeaderBinding
import kr.co.hs.sudoku.databinding.LayoutListItemMatrixTitleBinding
import java.io.InvalidClassException

sealed class MatrixListItemViewHolder<T : MatrixListItem>(itemView: View) : ViewHolder(itemView) {
    companion object {
        inline fun <reified T : MatrixListItem> create(
            inflater: LayoutInflater,
            parent: ViewGroup
        ) = when (T::class.java) {
            MatrixListItem.MatrixItem::class.java ->
                Matrix(LayoutListItemMatrixBinding.inflate(inflater, parent, false))

            MatrixListItem.HeaderItem::class.java ->
                Header(LayoutListItemMatrixHeaderBinding.inflate(inflater, parent, false))

            MatrixListItem.TitleItem::class.java ->
                Title(LayoutListItemMatrixTitleBinding.inflate(inflater, parent, false))

            else -> throw InvalidClassException("invalid type")
        }
    }

    abstract val clickableView: View

    abstract fun onBind(item: T)

    fun setOnClickListener(l: OnClickListener) = clickableView.setOnClickListener(l)

    class Matrix(private val binding: LayoutListItemMatrixBinding) :
        MatrixListItemViewHolder<MatrixListItem.MatrixItem>(binding.root) {

        override val clickableView: View by lazy { binding.cardView }

        override fun onBind(item: MatrixListItem.MatrixItem) = with(binding.matrix) {
            matrix = item.matrix
            invalidate()
        }

    }

    class Title(private val binding: LayoutListItemMatrixTitleBinding) :
        MatrixListItemViewHolder<MatrixListItem.TitleItem>(binding.root) {
        override val clickableView: View by lazy { binding.tvTitle }
        override fun onBind(item: MatrixListItem.TitleItem) = with(binding.tvTitle) {
            text = item.title
        }
    }

    class Header(private val binding: LayoutListItemMatrixHeaderBinding) :
        MatrixListItemViewHolder<MatrixListItem.HeaderItem>(binding.root) {

        override val clickableView: View by lazy { binding.cardView }
        override fun onBind(item: MatrixListItem.HeaderItem) = with(binding.tvHeader) {
            text = item.header
        }
    }
}