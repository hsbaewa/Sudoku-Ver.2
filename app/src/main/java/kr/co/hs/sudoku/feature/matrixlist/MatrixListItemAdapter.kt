package kr.co.hs.sudoku.feature.matrixlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import kr.co.hs.sudoku.model.matrix.IntMatrix

class MatrixListItemAdapter(
    private val onItemClick: (IntMatrix) -> Unit
) : ListAdapter<MatrixListItem, MatrixListItemViewHolder<out MatrixListItem>>(
    MatrixListItemDiffCallback()
) {

    companion object {
        const val VT_HEADER = 1010
        const val VT_ITEM = 1020
        const val VT_TITLE = 1030
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MatrixListItemViewHolder<out MatrixListItem> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VT_TITLE ->
                MatrixListItemViewHolder.create<MatrixListItem.TitleItem>(inflater, parent)

            VT_HEADER ->
                MatrixListItemViewHolder.create<MatrixListItem.HeaderItem>(inflater, parent)

            VT_ITEM ->
                MatrixListItemViewHolder.create<MatrixListItem.MatrixItem>(inflater, parent)
                    .apply {
                        setOnClickListener {
                            (getItem(bindingAdapterPosition) as? MatrixListItem.MatrixItem)
                                ?.matrix
                                ?.let(onItemClick)
                        }
                    }

            else -> throw Exception("invalid type")
        }
    }

    override fun onBindViewHolder(
        holder: MatrixListItemViewHolder<out MatrixListItem>,
        position: Int
    ) {
        when (val item = getItem(position)) {
            is MatrixListItem.HeaderItem -> (holder as MatrixListItemViewHolder.Header).onBind(item)
            is MatrixListItem.MatrixItem -> (holder as MatrixListItemViewHolder.Matrix).onBind(item)
            is MatrixListItem.TitleItem -> (holder as MatrixListItemViewHolder.Title).onBind(item)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is MatrixListItem.HeaderItem -> VT_HEADER
        is MatrixListItem.MatrixItem -> VT_ITEM
        is MatrixListItem.TitleItem -> VT_TITLE
    }
}