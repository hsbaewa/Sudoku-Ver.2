package kr.co.hs.sudoku.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import kr.co.hs.sudoku.R
import kotlin.math.sqrt

class Board : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        attrs?.initAttributes()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        attrs?.initAttributes()
    }

    private fun AttributeSet.initAttributes() {
        val typeArray = context.obtainStyledAttributes(this, R.styleable.Board)

        numberTextColorResId = typeArray.getColor(R.styleable.Board_numberColor, Color.BLACK)
        rowValueCount = typeArray.getInt(R.styleable.Board_rowCount, 0)


        backgroundColorResId =
            typeArray.getColor(R.styleable.Board_backgroundColor, Color.WHITE)
        borderColorResId = typeArray.getColor(R.styleable.Board_borderColor, Color.BLACK)
        accentColorResId = typeArray.getColor(R.styleable.Board_accentColor, Color.WHITE)

        typeArray.recycle()

        background = GradientDrawable().apply {
            setColor(backgroundColorResId)
            setStroke(4, borderColorResId)
        }

        if (rowValueCount > 0) {
            setRowValueCount(rowValueCount)
        }
    }

    private var numberTextColorResId = 0
    private var backgroundColorResId = 0
    private var borderColorResId = 0
    private var rowValueCount = 0
    private var accentColorResId = 0

    fun setRowValueCount(count: Int) {
        removeAllViews()
        val numberMap = List(count) { row ->
            List(count) { column ->
                val selector = NumberSelector(context)
                    .apply {
                        id = View.generateViewId()
                        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        setTextColor(numberTextColorResId)
                    }

                val background = GradientDrawable()
                background.setStroke(1, borderColorResId)
                if (((row / sqrt(rowValueCount.toDouble()).toInt()) + (column / sqrt(rowValueCount.toDouble()).toInt())) % 2 == 0) {
                    background.setColor(accentColorResId)
                }

                val rippleStateList = ColorStateList.valueOf(Color.BLUE)
                val mask = GradientDrawable()
                mask.setColor(Color.RED)
                selector.background = RippleDrawable(rippleStateList, background, mask)


                addView(selector)
                selector
            }
        }

        val set = ConstraintSet()
        set.clone(this)

        numberMap.forEachIndexed { row, columns ->
            columns.forEachIndexed { column, unit ->
                if (row > 0) {
                    set.connect(unit.id, TOP, numberMap[row - 1][column].id, BOTTOM)
                } else {
                    set.connect(unit.id, TOP, PARENT_ID, TOP)
                }

                if (row < count - 1) {
                    set.connect(unit.id, BOTTOM, numberMap[row + 1][column].id, TOP)
                } else {
                    set.connect(unit.id, BOTTOM, PARENT_ID, BOTTOM)
                }

                if (column > 0) {
                    set.connect(unit.id, START, numberMap[row][column - 1].id, END)
                } else {
                    set.connect(unit.id, START, PARENT_ID, START)
                }

                if (column < count - 1) {
                    set.connect(unit.id, END, numberMap[row][column + 1].id, START)
                } else {
                    set.connect(unit.id, END, PARENT_ID, END)
                }

                set.constrainedWidth(unit.id, true)
                set.constrainedHeight(unit.id, true)
            }
        }

        set.applyTo(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val sizeSpec = when {
            widthSize < heightSize -> MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY)
            else -> MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY)
        }

        super.onMeasure(sizeSpec, sizeSpec)
    }

}