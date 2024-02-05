package kr.co.hs.sudoku.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getDimensionPixelSizeOrThrow
import androidx.core.content.res.getIntOrThrow
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.platform.ContextExtension.getColorCompat
import kotlin.math.sqrt

open class MatrixItemView : View {
    constructor(context: Context?) : super(context) {
        outlineDividerWidth = 2.dp
        dividerWidth = 1.dp
        dividerColor = context?.getColorCompat(R.color.gray_600) ?: 0
        disabledCellColor = context?.getColorCompat(R.color.gray_400) ?: 0
        enabledCellColor = context?.getColorCompat(R.color.white) ?: 0
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        attrs?.initAttributes()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        attrs?.initAttributes()
    }

    @Suppress("unused")
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        attrs?.initAttributes()
    }

    private fun AttributeSet.initAttributes() {
        val typeArray = context.obtainStyledAttributes(this, R.styleable.MatrixItemView)
        outlineDividerWidth = typeArray
            .runCatching { getDimensionPixelSizeOrThrow(R.styleable.MatrixItemView_matrix_outline_thickness) }
            .getOrNull()
            ?.toFloat()
            ?: 2.dp

        dividerWidth = typeArray
            .runCatching { getDimensionPixelSizeOrThrow(R.styleable.MatrixItemView_matrix_divider_thickness) }
            .getOrNull()
            ?.toFloat()
            ?: 1.dp

        dividerColor = typeArray
            .runCatching { getColorOrThrow(R.styleable.MatrixItemView_matrix_border_color) }
            .getOrNull()
            ?: context.getColorCompat(R.color.gray_600)

        disabledCellColor = typeArray
            .runCatching { getColorOrThrow(R.styleable.MatrixItemView_matrix_disabled_color) }
            .getOrNull()
            ?: context.getColorCompat(R.color.gray_400)

        enabledCellColor = typeArray
            .runCatching { getColorOrThrow(R.styleable.MatrixItemView_matrix_enabled_color) }
            .getOrNull()
            ?: context.getColorCompat(R.color.white)

        typeArray
            .runCatching { getIntOrThrow(R.styleable.MatrixItemView_matrix_size) }
            .getOrNull()
            ?.run { setMatrixSize(this) }

        typeArray.recycle()
    }

    private var outlineDividerWidth: Float = 0f
    private var dividerWidth: Float = 0f
    private var dividerColor: Int = 0
    private var disabledCellColor: Int = 0
    private var enabledCellColor: Int = 0


    private val paint: Paint by lazy { Paint() }
    private val rectF: RectF by lazy { RectF() }

    protected val matrixValues: MutableList<MutableList<Int>> = mutableListOf()
    protected fun getColumnSize() = matrixValues.firstOrNull()?.size ?: 0
    protected fun getRowSize() = matrixValues.size

    private var matrixFixedValues: List<List<Int>>? = null

    open fun setMatrixSize(size: Int) {
        val bufferMatrix = MutableList(size) { MutableList(size) { 0 } }
        synchronized(matrixValues) {
            matrixValues.clear()
            matrixValues.addAll(bufferMatrix)
        }
    }

    fun setFixedCellValues(list: List<List<Int>>) {
        if (matrixValues.size != list.size) {
            setMatrixSize(list.size)
        }
        this.matrixFixedValues = list
        list.forEachIndexed { row, ints ->
            ints.forEachIndexed { column, value ->
                if (value != 0) {
                    matrixValues[row][column] = value
                }
            }
        }
    }

    fun setCellValue(row: Int, column: Int, value: Int) {
        matrixValues[row][column] = value
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        with(canvas) {
            matrixValues.forEachIndexed { row, list ->
                list.forEachIndexed { column, value ->
                    drawCell(row, column, value)
                }
            }
            drawOutline()
            drawDivider()
        }
    }

    private fun Canvas.drawCell(row: Int, column: Int, value: Int) {
        rectF.setMatrixPosition(row, column)
        paint.apply {
            flags = ANTI_ALIAS_FLAG
            style = Paint.Style.FILL
            color = if (isFixedCell(row, column)) disabledCellColor else enabledCellColor
        }
        drawRect(rectF, paint)
        onDrawCell(this, row, column, value, rectF)
    }

    protected fun RectF.setMatrixPosition(row: Int, column: Int) = apply {
        val w = width.toFloat()
        val h = height.toFloat()
        val columnSize = getColumnSize()
        val rowSize = getRowSize()

        set(
            w.div(columnSize).times(column),
            h.div(rowSize).times(row),
            w.div(columnSize).times(column + 1),
            h.div(rowSize).times(row + 1)
        )
    }

    protected open fun onDrawCell(
        canvas: Canvas,
        row: Int,
        column: Int,
        value: Int,
        rectF: RectF
    ) {
    }

    private fun Canvas.drawOutline() {
        rectF.set(
            outlineDividerWidth.div(2),
            outlineDividerWidth.div(2),
            width.toFloat().minus(outlineDividerWidth.div(2)),
            height.toFloat().minus(outlineDividerWidth.div(2))
        )
        drawRect(rectF, paint.apply {
            flags = ANTI_ALIAS_FLAG
            style = Paint.Style.STROKE
            color = dividerColor
            strokeWidth = outlineDividerWidth
        })
    }

    private fun Canvas.drawDivider() {
        val column = getColumnSize()
        val row = getRowSize()
        val columnSqrt = sqrt(column.toFloat()).toInt()
        val rowSqrt = sqrt(row.toFloat()).toInt()

        with(paint) {
            flags = ANTI_ALIAS_FLAG
            style = Paint.Style.STROKE
            color = dividerColor
        }

        (0 until column).forEach {
            val isBold = (it).rem(columnSqrt) == 0
            paint.strokeWidth = if (isBold) {
                outlineDividerWidth
            } else {
                dividerWidth
            }
            drawLine(
                width.toFloat().div(column).times(it),
                0f,
                width.toFloat().div(column).times(it),
                height.toFloat(),
                paint
            )
        }

        (0 until row).forEach {
            val isBold = (it).rem(rowSqrt) == 0
            paint.strokeWidth = if (isBold) {
                outlineDividerWidth
            } else {
                dividerWidth
            }
            drawLine(
                0f,
                height.toFloat().div(row).times(it),
                width.toFloat(),
                height.toFloat().div(row).times(it),
                paint
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 정사각형
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        when {
            width == 0 -> super.onMeasure(heightMeasureSpec, heightMeasureSpec)
            height == 0 -> super.onMeasure(widthMeasureSpec, widthMeasureSpec)
            width > height -> super.onMeasure(heightMeasureSpec, heightMeasureSpec)
            else -> super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        }
    }

    fun isFixedCell(row: Int, column: Int) = matrixFixedValues
        ?.runCatching { get(row)[column] }
        ?.getOrDefault(0)
        ?.run { this > 0 }
        ?: false
}