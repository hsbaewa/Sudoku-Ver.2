package kr.co.hs.sudoku.core

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kr.co.hs.sudoku.App

abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val app: App by lazy { itemView.context.applicationContext as App }
    fun getDrawableCompat(resId: Int) = ContextCompat.getDrawable(itemView.context, resId)
}