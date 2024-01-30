package kr.co.hs.sudoku.feature.profile

import android.view.View
import kr.co.hs.sudoku.core.ViewHolder

abstract class UserListItemViewHolder<T : UserListItem>(itemView: View) : ViewHolder(itemView) {
    abstract fun onBind(item: T)
}