package kr.co.hs.sudoku.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.Gravity.NO_GRAVITY
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.children
import androidx.core.widget.TextViewCompat

internal class NumberSelector : AppCompatTextView {


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        gravity = Gravity.CENTER
        TextViewCompat.setAutoSizeTextTypeWithDefaults(
            this,
            TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
        )
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> onTouchActionDown(event)
            MotionEvent.ACTION_MOVE -> onTouchActionMove(event)
            MotionEvent.ACTION_UP -> onTouchActionUp(event)
        }
        return true
    }

    private fun onTouchActionDown(event: MotionEvent) {
        lastTouchDownX = event.rawX.toInt() - (numberPadPopupWindow.width / 2)
        lastTouchDownY = event.rawY.toInt() - (numberPadPopupWindow.height / 2)
        numberPadPopupWindow.showAtLocation(rootView, NO_GRAVITY, lastTouchDownX, lastTouchDownY)
    }

    private var lastTouchDownX = 0
    private var lastTouchDownY = 0
    private val numberPadPopupWindow = NumberPadPopupWindow(context)


    private fun onTouchActionMove(event: MotionEvent) {
        val x = event.rawX.toInt() - lastTouchDownX
        val y = event.rawY.toInt() - lastTouchDownY

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

    private val rect = Rect()

    private fun onTouchActionUp(event: MotionEvent) {
        val x = event.rawX.toInt() - lastTouchDownX
        val y = event.rawY.toInt() - lastTouchDownY

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

    private fun PopupWindow.children() = (contentView as? ViewGroup)?.children ?: sequence { }
}