package kr.co.hs.sudoku.view

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.PopupWindow
import android.widget.TextView
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.ViewNumberpadBinding

class NumberPadPopupWindow(context: Context) : PopupWindow(), OnClickListener {

    init {
        contentView = onCreateView(context)
        width = 400
        height = 650
    }

    private fun onCreateView(context: Context) =
        ViewNumberpadBinding.inflate(LayoutInflater.from(context))
            .also {
                arrayOf(
                    it.btn01,
                    it.btn02,
                    it.btn03,
                    it.btn04,
                    it.btn05,
                    it.btn06,
                    it.btn07,
                    it.btn08,
                    it.btn09
                ).forEachIndexed { index, button ->
                    button.setOnClickListener(this)
                    button.text = (index.plus(1)).toString()
                    button.tag = index.plus(1)
                    button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20F)
                }

                it.btnDel.setOnClickListener(this)
            }
            .root


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn01 -> {
                contentView.findViewById<TextView>(R.id.tvPreView)
                    .text = "1"
            }
            R.id.btn02 -> {
                contentView.findViewById<TextView>(R.id.tvPreView)
                    .text = "2"
            }
        }
    }

    fun setPreView(text: String) {
        contentView.findViewById<TextView>(R.id.tvPreView).text = text
    }
}