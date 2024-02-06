package kr.co.hs.sudoku.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity.NO_GRAVITY
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.PopupWindow
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.getColorOrThrow
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.extension.platform.ContextExtension.getColorCompat
import kotlin.math.ceil

class SudokuView : MatrixItemView {

    companion object {
        private const val TAG = "SudokuView"

        @Suppress("unused")
        private fun debug(message: String) = Log.d(TAG, message)
    }

    constructor(context: Context?) : super(context)
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

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        attrs?.initAttributes()
    }

    private fun AttributeSet.initAttributes() {
        val typeArray = context.obtainStyledAttributes(this, R.styleable.SudokuView)
        textColor = typeArray
            .runCatching { getColorOrThrow(R.styleable.SudokuView_matrix_text_color) }
            .getOrDefault(context.getColorCompat(R.color.gray_700))

        typeArray.recycle()
    }

    data class CellPosition(val row: Int, val column: Int)

    private var textColor: Int = 0
    private val paint: Paint by lazy { Paint() }
    private val rectF: RectF by lazy { RectF() }

    private var currentCellPosition: CellPosition? = null
    var enabledHapticFeedback = true
    private var matrixErrorValues: MutableList<MutableList<Int>>? = null
    private var onCellValueChangedListener: CellValueChangedListener? = null

    override fun setMatrixSize(size: Int) {
        super.setMatrixSize(size)
        val bufferMatrix = MutableList(size) { MutableList(size) { 0 } }
        matrixErrorValues = bufferMatrix
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        currentCellPosition?.run { canvas.drawCross(row, column) }
    }

    override fun onDrawCell(canvas: Canvas, row: Int, column: Int, value: Int, rectF: RectF) {
        if (value == 0)
            return

        if (isErrorCell(row, column)) {
            paint.apply {
                flags = ANTI_ALIAS_FLAG
                style = Paint.Style.FILL
                color = context.getColorCompat(R.color.error_font_background)
            }
            canvas.drawRect(rectF, paint)
        }

        if (!isVisibleNumber)
            return

        with(paint) {
            flags = ANTI_ALIAS_FLAG
            textAlign = Paint.Align.CENTER
            typeface = ResourcesCompat.getFont(context, R.font.goreyong_ddalgi)
            textSize = rectF.width().times(0.8f)
            color = when {
                isErrorCell(row, column) -> context.getColorCompat(R.color.error_font)
                isFixedCell(row, column) -> context.getColorCompat(R.color.white)
                else -> textColor
            }
        }

        val metric = paint.fontMetrics
        val textHeight = ceil(metric.descent - metric.ascent).toInt()
        val y = textHeight - metric.descent
        canvas.drawText(value.toString(), rectF.centerX(), rectF.centerY().plus(y.div(2.3f)), paint)
    }

    private fun isErrorCell(row: Int, column: Int) = matrixErrorValues
        ?.runCatching { get(row)[column] == 1 }
        ?.getOrDefault(false)
        ?: false

    private fun Canvas.drawCross(row: Int, column: Int) {
        paint.apply {
            flags = ANTI_ALIAS_FLAG
            style = Paint.Style.FILL
            color = context.getColorCompat(R.color.sudoku_cross)
        }
        for (x in 0 until getColumnSize()) {
            if (x == column)
                continue
            rectF.setMatrixPosition(row, x)
            drawRect(rectF, paint)
        }

        for (y in 0 until getRowSize()) {
            if (y == row)
                continue
            rectF.setMatrixPosition(y, column)
            drawRect(rectF, paint)
        }

        rectF.setMatrixPosition(row, column)
        drawRect(rectF, paint)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        return when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                // 셀의 중앙 좌표 값 구하기
                val x = event.x
                val y = event.y
                val cellPosition = getCellPosition(x, y)

                if (isFixedCell(cellPosition.row, cellPosition.column))
                    return false

                showNumberSelection(event.rawX, event.rawY)
                performHapticFeedback()

                // 선택한 셀 기준으로 크로스 표시를 위한 갱신
                currentCellPosition = cellPosition
                invalidate()
                true
            }

            MotionEvent.ACTION_MOVE -> {
                selectionWindow.touch(event.rawX, event.rawY)
                true
            }

            MotionEvent.ACTION_UP -> {
                val selectedNumber = getNumberSelected()
                currentCellPosition?.run {
                    matrixValues[row][column] = selectedNumber
                    onCellValueChangedListener?.onChangedCell(
                        row,
                        column,
                        selectedNumber.takeIf { it > 0 }
                    )
                }
                currentCellPosition = null
                invalidate()
                true
            }

            else -> super.dispatchTouchEvent(event)
        }
    }

    private fun getCellPosition(x: Float, y: Float) =
        CellPosition(
            y.div(height.div(getRowSize())).toInt(),
            x.div(width.div(getColumnSize())).toInt()
        )

    private fun View.performHapticFeedback() =
        enabledHapticFeedback
            .takeIf { it }
            ?.run { performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) }


    /**
     * 숫자 선택 윈도우 관련
     */
    private val selectionWindow: NumberSelectionWindow
            by lazy { NumberSelectionWindow() }

    private fun showNumberSelection(x: Float, y: Float) = with(selectionWindow) {
        dismiss()
        showAtPosition(x, y)
    }

    private fun getNumberSelected() = with(selectionWindow) {
        dismiss()
        when (val action = getCurrentAction()) {
            is NumberSelectionView.Number -> action.number
            else -> 0
        }
    }

    private inner class NumberSelectionWindow : PopupWindow() {
        init {
            contentView = NumberSelectionView(context).apply {
                setNumberCount(getRowSize())
                enabledHapticFeedback = this@SudokuView.enabledHapticFeedback
            }
            width = 500
            height = 500
        }

        fun showAtPosition(x: Float, y: Float) = showAtLocation(
            this@SudokuView,
            NO_GRAVITY,
            x.toInt().minus(width.div(2)),
            y.toInt().minus(height.div(2))
        )

        fun getCurrentAction() = (contentView as NumberSelectionView).getCurrentAction()

        fun touch(x: Float, y: Float) =
            (selectionWindow.contentView as? NumberSelectionView)?.touch(x, y)
    }


    /**
     * 셀 값 변경 이벤트
     */
    interface CellValueChangedListener {
        fun onChangedCell(row: Int, column: Int, value: Int?)
    }

    fun setOnCellValueChangedListener(l: CellValueChangedListener?) {
        this.onCellValueChangedListener = l
    }

    var isVisibleNumber: Boolean = false
        set(value) {
            field = value
            invalidate()
        }


    fun setError(row: Int, column: Int) {
        matrixErrorValues
            ?.runCatching { get(row)[column] = 1 }
            ?.getOrNull()
    }

    fun removeError(row: Int, column: Int) {
        matrixErrorValues
            ?.runCatching { get(row)[column] = 0 }
            ?.getOrNull()
    }

    @Suppress("unused")
    fun clearError() {
        matrixErrorValues?.clear()
    }

    fun getMatrixValue(row: Int, column: Int) = matrixValues[row][column]
}