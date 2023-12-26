package kr.co.hs.sudoku.core

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun getDrawableCompat(resId: Int) = ContextCompat.getDrawable(itemView.context, resId)
    fun getColorCompat(resId: Int) = ContextCompat.getColor(itemView.context, resId)
    fun getString(resId: Int) = itemView.context.getString(resId)
}