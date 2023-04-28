package kr.co.hs.sudoku.views

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import androidx.appcompat.widget.AppCompatTextView
import kr.co.hs.sudoku.R

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
            if (number == 0) {
                text = context.getString(R.string.start)
            } else {
                text = number.toString()
            }
            startCountDownAnimation {
                var currentNumber = text.toString().toIntOrNull()
                currentNumber?.let {
                    if (currentNumber-- > 0) {
                        start(currentNumber, onAfter)
                    } else {
                        onAfter?.invoke()
                    }
                } ?: onAfter?.invoke()

            }
        }
    }

    private inline fun startCountDownAnimation(
        crossinline onAnimationEnd: () -> Unit
    ) {
        val allDuration = 1000L
        val hideDuration = 100L

        val showAnim = popAnimation(allDuration - hideDuration)
        val extendAnim = extendAnimation(showAnim.duration, hideDuration)
        val hideAnim = hideAnimation(extendAnim.startOffset, extendAnim.duration)

        val animSet = AnimationSet(true)
        animSet.addAnimation(showAnim)
        animSet.addAnimation(extendAnim)
        animSet.addAnimation(hideAnim)

        animSet.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) = onAnimationEnd()
            override fun onAnimationRepeat(p0: Animation?) {}
        })
        startAnimation(animSet)
    }

    private fun popAnimation(duration: Long) = ScaleAnimation(
        0f, 1f, 0f, 1f,
        RELATIVE_TO_SELF, .5f, // Pivot point of X scaling
        RELATIVE_TO_SELF, .5f
    ).also {
        it.duration = duration
        it.interpolator = AccelerateDecelerateInterpolator()
    }

    private fun extendAnimation(offset: Long, duration: Long) = ScaleAnimation(
        1f, 2f, 1f, 2f,
        RELATIVE_TO_SELF, .5f, // Pivot point of X scaling
        RELATIVE_TO_SELF, .5f
    ).also {
        it.duration = duration
        it.startOffset = offset
        it.interpolator = AccelerateInterpolator()
    }

    private fun hideAnimation(offset: Long, duration: Long) = AlphaAnimation(1f, 0f).also {
        it.startOffset = offset
        it.duration = duration
        it.interpolator = AccelerateInterpolator()
    }

}