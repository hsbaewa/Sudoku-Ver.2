package kr.co.hs.sudoku.view

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.children

class NumberSelector : AppCompatTextView {


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val numberPadPopupWindow: NumberPadPopupWindow = NumberPadPopupWindow(context)

    private val rect = Rect()
    private var layoutCenter: Point? = null


    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                layoutCenter = event.getCenterPoint(numberPadPopupWindow)
                    .also { numberPadPopupWindow.showAtLocation(it) }
            }
            MotionEvent.ACTION_MOVE -> {
                val x = event.rawX.toInt() - (layoutCenter?.x ?: 0)
                val y = event.rawY.toInt() - (layoutCenter?.y ?: 0)


                with(rect) {
                    numberPadPopupWindow
                        .children()
                        .forEach {
                            it.getHitRect(this)
                            takeIf { contains(x, y) }
                                ?.run {
                                    it.isPressed = true
                                    numberPadPopupWindow.setPreView(it.tag?.toString() ?: "")
                                }
                                ?: kotlin.run {
                                    it.isPressed = false
                                }
                        }
                }

            }
            MotionEvent.ACTION_UP -> {
                val x = event.rawX.toInt() - (layoutCenter?.x ?: 0)
                val y = event.rawY.toInt() - (layoutCenter?.y ?: 0)

                with(rect) {
                    numberPadPopupWindow.children()
                        .find {
                            it.getHitRect(this)
                            contains(x, y)
                        }
                }?.run {
                    this@NumberSelector.text = tag?.toString() ?: ""
                    performClick()
                }

                numberPadPopupWindow.dismiss()
            }
        }
        return true
    }

    private fun MotionEvent.getCenterPoint(window: PopupWindow) =
        Point(rawX.toInt() - (window.width / 2), rawY.toInt() - (window.height / 2))

    private fun PopupWindow.showAtLocation(position: Point) =
        showAtLocation(
            (context as Activity).window.decorView.rootView,
            Gravity.NO_GRAVITY,
            position.x,
            position.y - 20
        )

    private fun PopupWindow.children() = (contentView as? ViewGroup)?.children ?: sequence { }

}