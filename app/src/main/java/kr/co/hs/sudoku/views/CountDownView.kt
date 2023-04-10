package kr.co.hs.sudoku.views

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.ScaleAnimation
import androidx.appcompat.widget.AppCompatTextView

class CountDownView : AppCompatTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun start(number: Int, onAfter: (() -> Unit)? = null) {
        post {
            text = number.toString()
            startCountDownAnimation {
                var currentNumber = text.toString().toInt()
                if (currentNumber-- > 1) {
                    start(currentNumber, onAfter)
                } else {
                    onAfter?.invoke()
                }
            }
        }
    }

    private inline fun startCountDownAnimation(
        crossinline onAnimationEnd: () -> Unit
    ) {
        val anim = ScaleAnimation(
            3f, 0f, 3f, 0f,
            Animation.RELATIVE_TO_SELF, .5f, // Pivot point of X scaling
            Animation.RELATIVE_TO_SELF, .5f
        )
        anim.fillAfter = true
        anim.duration = 1000
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) = onAnimationEnd()
            override fun onAnimationRepeat(p0: Animation?) {}
        })
        startAnimation(anim)
    }
}