package kr.co.hs.sudoku.views

import android.annotation.SuppressLint
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
    private val selectionWindow: NumberSelectionWindow
            by lazy { NumberSelectionWindow() }
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
                val x = event.x
                val y = event.y

                selectionWindow.dismiss()
                val cellPosition = getCellPosition(x, y)

                if (isFixedCell(cellPosition.row, cellPosition.column))
                    return false

                currentCellPosition = cellPosition
                val rectF = RectF()
                rectF.setMatrixPosition(cellPosition.row, cellPosition.column)

                val xterm = event.rawX - x
                val yterm = event.rawY - y

                selectionWindow.showAtPosition(
                    rectF.centerX() + xterm,
                    rectF.centerY() + yterm - statusBarHeight
                )

                performHapticFeedback()

                invalidate()

                true
            }

            MotionEvent.ACTION_MOVE -> {
                with(selectionWindow.contentView as NumberSelectionView) {
                    val intArray = IntArray(2)
                    getLocationOnScreen(intArray)
                    val start = intArray[0]
                    val top = intArray[1]

                    val x = event.rawX
                    val y = event.rawY

                    touch(
                        x = x.toDouble().minus(start.toDouble()),
                        y = y.toDouble().minus(top.toDouble())
                    )

                }

//                currentCellPosition?.run {
//                    matrixValues[row].set(
//                        column,
//                        selectionWindow.getCurrentNumber() ?: 0
//                    )
//                }
//                invalidate()

                true
            }

            MotionEvent.ACTION_UP -> {
                selectionWindow.dismiss()
                currentCellPosition?.run {
                    val cellValue = selectionWindow.getCurrentNumber() ?: 0
                    matrixValues[row][column] = cellValue

                    onCellValueChangedListener?.onChangedCell(
                        row,
                        column,
                        cellValue.takeIf { it > 0 }
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

    private fun getCellPosition(event: MotionEvent): CellPosition {
        val x = event.x
        val rawX = event.rawX
        val y = event.y
        val rawY = event.rawY
        return CellPosition(
            (y - (rawY - y)).div(height.div(getRowSize())).toInt(),
            (x - (rawX - x)).div(width.div(getColumnSize())).toInt()
        )
    }


    private inner class NumberSelectionWindow : PopupWindow() {
        init {
            contentView = NumberSelectionView(context).apply {
                startNumber = 1
                numberCount = getRowSize()
                enabledHapticFeedback = this@SudokuView.enabledHapticFeedback
            }
            width = 500
            height = 500
        }

        fun showAtPosition(x: Float, y: Float) {
            val centerX = calculatePopupCenterX(x)
            val centerY = calculatePopupCenterY(y)
            val rootX = this@SudokuView.x
            val rootY = this@SudokuView.y
            println(rootX + rootY)
            showAtLocation(
                this@SudokuView,
                NO_GRAVITY,
                centerX.plus(rootX).toInt(),
                centerY.plus(rootY).toInt()
            )
        }

        fun getCurrentNumber() = (contentView as NumberSelectionView).getCurrentNumber()
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/03/31
     * @comment 터치를 기준으로 팝업의 center x 좌표 계산, 화면 크기및 boardview를 기준으로 좌, 우 끝부분인 경우 팝업이 밀려서 중간 좌표를 계산해야한다.
     * @param touchX 터치한 x 좌표
     * @return 계산된 결과 X
     **/
    private fun PopupWindow.calculatePopupCenterX(touchX: Float): Float {
        // 팝업의 center x 좌표
        val popupCenterX = width.toFloat() / 2
        // 화면 너비
        val windowWidth = getWindowSize().first
        val popupXOffset = when {
            // 화면 좌측 끝을 터치했을 때
            popupCenterX - touchX > 0 -> popupCenterX - touchX
            // 화면 우측 끝을 터치했을 때 화면 사이즈 보다 팝업이 넘어가는 경우 팝업이 자동으로 화면 끝에 붙으므로 밀린 만큼 빼야됨.
            popupCenterX + touchX > windowWidth -> windowWidth - (popupCenterX + touchX)
            // 중간 터치
            else -> 0F
        }
        return (touchX - popupCenterX) + popupXOffset
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/03/31
     * @comment 터치를 기준으로 팝업의 center y 좌표 계산, 화면 크기및 boardview를 기준으로 상, 하 끝부분인 경우 팝업이 밀려서 중간 좌표를 계산해야한다.
     * @param touchY 터치한 y 좌표
     * @return 계산 결과
     **/

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    private fun PopupWindow.calculatePopupCenterY(touchY: Float): Float {
        val popupCenterY = height.toFloat() / 2
        val windowHeight = getWindowSize().second

        val popupYOffset = when {
            popupCenterY - touchY > 0 -> popupCenterY - touchY
            popupCenterY + touchY > windowHeight -> windowHeight - (popupCenterY + touchY)
            else -> 0F
        }

        return (touchY - popupCenterY) + popupYOffset + statusBarHeight
    }

    private fun getWindowSize() =
        context.resources.displayMetrics.widthPixels.toFloat() to context.resources.displayMetrics.heightPixels.toFloat()


    private val statusBarHeight: Int
            by lazy { resources.getDimensionPixelSize(statusBarHeightIdentifier) }


    private val statusBarHeightIdentifier: Int
            by lazy { resources.getIdentifier("status_bar_height", "dimen", "android") }

    private fun View.performHapticFeedback() =
        enabledHapticFeedback
            .takeIf { it }
            ?.run { performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) }


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

    fun clearError() {
        matrixErrorValues?.clear()
    }

    private fun isErrorCell(row: Int, column: Int) = matrixErrorValues
        ?.runCatching { get(row)[column] == 1 }
        ?.getOrDefault(false)
        ?: false


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
}