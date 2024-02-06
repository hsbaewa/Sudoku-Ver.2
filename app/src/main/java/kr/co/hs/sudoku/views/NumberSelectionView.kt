package kr.co.hs.sudoku.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.core.content.res.ResourcesCompat
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.platform.ContextExtension.getColorCompat
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class NumberSelectionView : View {
    companion object {
        private const val TAG = "NumberSelectionView"
        fun debug(message: String) = Log.d(TAG, message)
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    data class Position(val x: Double, val y: Double) {
        fun distance(with: Position) =
            sqrt(x.minus(with.x).pow(2).plus(y.minus(with.y).pow(2)))
    }

    sealed interface Action {
        val idx: Int
        val displayValue: String
    }

    data class Number(override val idx: Int, val number: Int) : Action {
        override val displayValue: String
            get() = number.toString()
    }

    data class Clear(override val idx: Int, override val displayValue: String) : Action

    private val actionList = ArrayList<Action>()
    fun setNumberCount(count: Int) {
        synchronized(actionList) {
            with(actionList) {
                clear()
                (0 until count).forEach { add(Number(it, it + 1)) }
                add(Clear(count, context.getString(R.string.Clear)))
            }
        }
    }

    // 숫자가 시작 하는 지점을 설정(0도이면 우측 부터 시작)
    var startingOffsetRadian = -PI.div(2)

    private var currentSelection: Action? = null

    private val paint: Paint by lazy { Paint() }
    var enabledHapticFeedback = true

    private fun getCount(): Int {
        return actionList.size
    }

    // 가운데 좌표
    private val center: Position
        get() = Position(x = width.div(2.0), y = height.div(2.0))

    // 숫자판 배열 반지름
    private val radius: Float
        get() = width.div(4.minus(getCount().div(10f)))

    private val numberCircumference: Float
        get() = ((2 * PI * radius) / getCount()).toFloat().takeIf {
            it < 50.dp
        } ?: 50.dp

    private fun getNumberCenterPositionX(idx: Int) =
        cos(PI.times(2).div(getCount()).times(idx) + startingOffsetRadian).times(radius)

    private fun getNumberCenterPositionY(idx: Int) =
        sin(PI.times(2).div(getCount()).times(idx) + startingOffsetRadian).times(radius)

    private fun getNumberCenterPosition(center: Position, idx: Int) =
        Position(getNumberCenterPositionX(idx) + center.x, getNumberCenterPositionY(idx) + center.y)

    private fun getNumberCenterPosition(idx: Int) = getNumberCenterPosition(center, idx)

    override

    fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        currentSelection?.run { canvas.drawCenterCircle(displayValue) }
        actionList.forEach {
            canvas.drawNumberCircle(it)
            canvas.drawNumberText(it)
        }
    }

    private fun Canvas.drawCenterCircle(text: String) {
        drawCircle(
            center.x.toFloat(),
            center.y.toFloat(),
            radius.div(2),
            paint.apply {
                color = context.getColorCompat(R.color.gray_700)
            }
        )

        with(paint) {
            flags = ANTI_ALIAS_FLAG
            textAlign = Paint.Align.CENTER
            typeface = ResourcesCompat.getFont(context, R.font.goreyong_ddalgi)
            textSize = radius.times(0.8f)
            color = context.getColorCompat(R.color.white)
        }

        val metric = paint.fontMetrics
        val textHeight = ceil(metric.descent - metric.ascent).toInt()
        drawText(
            text,
            center.x.toFloat(),
            center.y.plus((textHeight - metric.descent).div(2.3f)).toFloat(),
            paint
        )
    }

    private fun Canvas.drawNumberCircle(action: Action) {
        with(paint) {
            color = if (currentSelection == action) {
                context.getColorCompat(R.color.gray_700_alpha70)
            } else {
                context.getColorCompat(R.color.gray_200_alpha70)
            }
        }

        val numberCenter = getNumberCenterPosition(center, action.idx)
        drawCircle(
            numberCenter.x.toFloat(),
            numberCenter.y.toFloat(),
            numberCircumference.div(2),
            paint
        )
    }

    private fun Canvas.drawNumberText(action: Action) {
        with(paint) {
            flags = ANTI_ALIAS_FLAG
            textAlign = Paint.Align.CENTER
            typeface = ResourcesCompat.getFont(context, R.font.goreyong_ddalgi)
            textSize = numberCircumference.times(0.8f)
            color = if (currentSelection == action) {
                context.getColorCompat(R.color.white_alpha70)
            } else {
                context.getColorCompat(R.color.gray_700_alpha70)
            }
        }

        val numberCenter = getNumberCenterPosition(center, action.idx)
        val metric = paint.fontMetrics
        val textHeight = ceil(metric.descent - metric.ascent).toInt()
        drawText(
            action.displayValue,
            numberCenter.x.toFloat(),
            numberCenter.y.plus((textHeight - metric.descent).div(2.3f)).toFloat(),
            paint
        )
    }

    fun touch(x: Float, y: Float) {
        val locationOnScreen = IntArray(2)
        getLocationOnScreen(locationOnScreen)

        val lastSelectionAction = currentSelection
        val positionX = x.minus(locationOnScreen[0])
        val positionY = y.minus(locationOnScreen[1])

        getAction(positionX.toDouble(), positionY.toDouble())?.let {
            if (it != lastSelectionAction) {
                performHapticFeedback()
            }
            currentSelection = it
        }

        invalidate()
    }

    private fun getAction(idx: Int) = actionList[idx]
    private fun getAction(x: Double, y: Double): Action? {
        val touchPosition = Position(x, y)
        var minimumDistance = Double.MAX_VALUE
        var minimumDistanceActionPosition: Int = -1
        (0 until getCount()).forEach {
            val actionPosition = getNumberCenterPosition(center, it)
            val distance = touchPosition.distance(actionPosition)
            if (distance < minimumDistance) {
                minimumDistance = distance
                minimumDistanceActionPosition = it
            }
        }
        return minimumDistanceActionPosition.takeIf { it >= 0 }?.run { getAction(this) }
    }

    fun getCurrentAction() = currentSelection

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 정사각형
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    private fun View.performHapticFeedback() =
        enabledHapticFeedback
            .takeIf { it }
            ?.run { performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) }
}