package kr.co.hs.sudoku.core

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import kr.co.hs.sudoku.databinding.LayoutListItemPagingLoadStateBinding

class PagingLoadStateAdapter : LoadStateAdapter<PagingLoadStateViewHolder>() {
    override fun onBindViewHolder(holder: PagingLoadStateViewHolder, loadState: LoadState) =
        holder.onBind()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): PagingLoadStateViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutListItemPagingLoadStateBinding.inflate(inflater, parent, false)
        return PagingLoadStateViewHolder(binding)
    }
}