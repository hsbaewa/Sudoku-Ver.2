package kr.co.hs.sudoku.feature.matrixlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import kr.co.hs.sudoku.core.ViewHolder
import kr.co.hs.sudoku.databinding.LayoutMatrixListHeaderBinding
import kr.co.hs.sudoku.databinding.LayoutMatrixListItemBinding
import kr.co.hs.sudoku.databinding.LayoutMatrixListTitleBinding
import kr.co.hs.sudoku.model.matrix.IntMatrix

class MatrixListItemAdapter(
    private val onItemClick: (IntMatrix) -> Unit
) : ListAdapter<MatrixListItem, ViewHolder>(MatrixListItemDiffCallback()) {

    companion object {
        const val VT_HEADER = 1010
        const val VT_ITEM = 1020
        const val VT_TITLE = 1030
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VT_TITLE -> MatrixListTitleViewHolder(
                LayoutMatrixListTitleBinding.inflate(inflater, parent, false)
            )

            VT_HEADER -> MatrixListHeaderViewHolder(
                LayoutMatrixListHeaderBinding.inflate(inflater, parent, false)
            )

            VT_ITEM -> MatrixListItemViewHolder(
                LayoutMatrixListItemBinding.inflate(inflater, parent, false)
            ).apply {
                binding.cardView.setOnClickListener {
                    (getItem(bindingAdapterPosition) as? MatrixListItem.MatrixItem)
                        ?.matrix
                        ?.let(onItemClick)
                }
            }

            else -> throw Exception("invalid type")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is MatrixListItem.HeaderItem -> (holder as MatrixListHeaderViewHolder).onBind(item)
            is MatrixListItem.MatrixItem -> (holder as MatrixListItemViewHolder).onBind(item.matrix)
            is MatrixListItem.TitleItem -> (holder as MatrixListTitleViewHolder).onBind(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is MatrixListItem.HeaderItem -> VT_HEADER
            is MatrixListItem.MatrixItem -> VT_ITEM
            is MatrixListItem.TitleItem -> VT_TITLE
        }
    }
}