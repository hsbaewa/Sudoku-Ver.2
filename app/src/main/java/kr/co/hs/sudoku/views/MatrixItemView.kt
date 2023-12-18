package kr.co.hs.sudoku.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.platform.ContextExtension.getColorCompat
import kotlin.math.sqrt

class MatrixItemView : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @Suppress("unused")
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    private val outlineDividerWidth: Float by lazy { 2.dp }
    private val dividerWidth: Float by lazy { 1.dp }
    private val dividerColor: Int by lazy { context.getColorCompat(R.color.gray_600) }
    private val disabledCellColor: Int by lazy { context.getColorCompat(R.color.gray_400) }
    private val enabledCellColor: Int by lazy { context.getColorCompat(R.color.white) }

    private val paintDivider: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = dividerColor
        }
    }
    private val paintDisabledCell: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = disabledCellColor
        }
    }
    private val paintEnabledCell: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = enabledCellColor
        }
    }
    var matrix: List<List<Int>>? = null
    private val rectF = RectF()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            this@MatrixItemView.matrix?.forEachIndexed { row, list ->
                list.forEachIndexed { column, value ->
                    drawCell(row, column, value == 0)
                }
            }
            drawOutline()
            drawDivider()
        }
    }

    private fun Canvas.drawCell(row: Int, column: Int, enabled: Boolean) {
        val columnSize = this@MatrixItemView.matrix?.firstOrNull()?.size ?: return
        val rowSize = this@MatrixItemView.matrix?.size?.takeIf { it > 0 } ?: return

        val w = width.toFloat()
        val h = height.toFloat()

        rectF.set(
            w.div(columnSize).times(column),
            h.div(rowSize).times(row),
            w.div(columnSize).times(column + 1),
            h.div(rowSize).times(row + 1)
        )
        drawRect(
            rectF, if (enabled) {
                paintEnabledCell
            } else {
                paintDisabledCell
            }
        )
    }

    private fun Canvas.drawOutline() {
        rectF.set(
            outlineDividerWidth.div(2),
            outlineDividerWidth.div(2),
            width.toFloat().minus(outlineDividerWidth.div(2)),
            height.toFloat().minus(outlineDividerWidth.div(2))
        )
        paintDivider.strokeWidth = outlineDividerWidth
        drawRect(rectF, paintDivider)
    }

    private fun Canvas.drawDivider() {
        val column = this@MatrixItemView.matrix?.firstOrNull()?.size ?: return
        val row = this@MatrixItemView.matrix?.size?.takeIf { it > 0 } ?: return
        val columnSqrt = sqrt(column.toFloat()).toInt()
        val rowSqrt = sqrt(row.toFloat()).toInt()

        (0 until column).forEach {
            val isBold = (it).rem(columnSqrt) == 0
            paintDivider.strokeWidth = if (isBold) {
                outlineDividerWidth
            } else {
                dividerWidth
            }
            drawLine(
                width.toFloat().div(column).times(it),
                0f,
                width.toFloat().div(column).times(it),
                height.toFloat(),
                paintDivider
            )
        }

        (0 until row).forEach {
            val isBold = (it).rem(rowSqrt) == 0
            paintDivider.strokeWidth = if (isBold) {
                outlineDividerWidth
            } else {
                dividerWidth
            }
            drawLine(
                0f,
                height.toFloat().div(row).times(it),
                width.toFloat(),
                height.toFloat().div(row).times(it),
                paintDivider
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 정사각형
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}