package kr.co.hs.sudoku.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.Gravity.NO_GRAVITY
import android.view.View.MeasureSpec.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import com.google.android.material.button.MaterialButton
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.extension.NumberExtension.toPx
import kr.co.hs.sudoku.extension.platform.TextViewExtension.isAutoSizeText
import kr.co.hs.sudoku.extension.platform.TextViewExtension.setAutoSizeText
import kr.co.hs.sudoku.extension.platform.TextViewExtension.setAutoSizeTextNone
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sqrt

class SudokuBoardView : ViewGroup {
    companion object {
        const val BORDER_WIDTH = 1
        const val BOX_BORDER_WIDTH = 3

        private fun log(message: String) = Log.d("SudokuBoardView", message)
    }

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
        val typeArray = context.obtainStyledAttributes(this, R.styleable.SudokuBoardView)

        numberTextColorResId =
            typeArray.getColor(R.styleable.SudokuBoardView_numberColor, Color.BLACK)
        rowValueCount = typeArray.getInt(R.styleable.SudokuBoardView_rowCount, 0)
        backgroundColorResId =
            typeArray.getColor(R.styleable.SudokuBoardView_backgroundColor, Color.WHITE)
        borderColorResId = typeArray.getColor(R.styleable.SudokuBoardView_borderColor, Color.BLACK)
        accentColorResId = typeArray.getColor(R.styleable.SudokuBoardView_accentColor, Color.WHITE)
        numberTextErrorColorResId =
            typeArray.getColor(R.styleable.SudokuBoardView_numberErrorColor, Color.RED)
        numberTextDisabledColorResId =
            typeArray.getColor(R.styleable.SudokuBoardView_numberDisabledColor, Color.WHITE)
        disabledColorResId =
            typeArray.getColor(R.styleable.SudokuBoardView_disabledColor, -1)

        typeArray.recycle()

        setBackgroundColor(borderColorResId)

        if (rowValueCount > 0) {
            setRowCount(rowValueCount)
        }
    }

    private var numberTextColorResId = 0
    private var backgroundColorResId = 0
    private var borderColorResId = 0
    private var rowValueCount = 0
    private var accentColorResId = 0
    private var numberTextErrorColorResId = 0
    private var numberTextDisabledColorResId = 0
    private var disabledColorResId = 0

    fun setRowCount(rowCount: Int, disabledMatrix: List<List<Int>>? = null) {
        this.rowValueCount = rowCount
        removeAllViews()
        numberPopup = NumberPadPopup(rowCount)
        createCellMatrix(rowCount, disabledMatrix)
    }

    private lateinit var numberPopup: NumberPadPopup

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/03/31
     * @comment 숫자 선택 팝업
     * @param maxNumber 최대 숫자
     **/
    private inner class NumberPadPopup(maxNumber: Int) : PopupWindow() {

        init {
            contentView = NumberPadView(context).apply {
                initMaxNumber(maxNumber)
            }
            width = 400
            height = 650
        }

        fun centerX() = width.toFloat() / 2
        fun centerY() = height.toFloat() / 2
        fun children() = (contentView as? ViewGroup)?.children ?: sequence { }
        fun setPreView(text: String) = (contentView as NumberPadView).setPreview(text)
        fun memoModeOn() = findMemoButton()?.run {
            isSelected = true
            text = context.getString(R.string.memo_status_on)
            setBackgroundColor(Color.LTGRAY)
        }

        private fun findMemoButton() = contentView.findViewWithTag<MaterialButton>(-1)
        fun memoModeOff() = findMemoButton()?.run {
            isSelected = false
            text = context.getString(R.string.memo_status_off)
            setBackgroundColor(backgroundColorResId)
        }

        fun isMemoMode() = findMemoButton()?.run { isSelected } ?: false
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/03/31
     * @comment 숫자 선택 팝업 레이아웃
     **/
    private inner class NumberPadView : ConstraintLayout {
        constructor(context: Context) : super(context)
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        )

        init {
            setBackgroundColor(Color.TRANSPARENT)
        }

        fun initMaxNumber(maxNumber: Int) {
            val set = ConstraintSet()
            set.clone(this)

            preview = createPreview()
            addView(preview)
            set.connect(preview.id, START, PARENT_ID, START)
            set.connect(preview.id, END, PARENT_ID, END)
            set.connect(preview.id, TOP, PARENT_ID, TOP)


            val deleteBtn = createDeleteButton()
            addView(deleteBtn)
            set.connect(deleteBtn.id, END, PARENT_ID, END)
            set.connect(deleteBtn.id, BOTTOM, PARENT_ID, BOTTOM)
//            set.connect(deleteBtn.id, START, PARENT_ID, START)
            // 우측으로 쏠리게 하고 싶으면 아래 주석 해제
//            set.constrainWidth(deleteBtn.id, WRAP_CONTENT)
//            set.setHorizontalBias(deleteBtn.id, 1f)

            val memoBtn = createMemoButton()
            addView(memoBtn)
            set.connect(memoBtn.id, START, PARENT_ID, START)
            set.connect(memoBtn.id, BOTTOM, PARENT_ID, BOTTOM)
            set.connect(memoBtn.id, END, deleteBtn.id, START)
            set.connect(deleteBtn.id, START, memoBtn.id, END)
//            set.constrainWidth(optionBtn.id, WRAP_CONTENT)


            val sideCount = ceil(sqrt(maxNumber.toDouble())).toInt()
            val map = List(sideCount) { row ->
                List(sideCount) { column ->
                    createNumberButton(row, column, sideCount).apply {
                        addView(this)
                    }
                }
            }

            map.forEachIndexed { row, columns ->
                columns.forEachIndexed { column, unit ->
                    if (row > 0) {
                        set.connect(unit.id, TOP, map[row - 1][column].id, BOTTOM)
                    } else {
                        set.connect(preview.id, BOTTOM, unit.id, TOP)
                        set.connect(unit.id, TOP, preview.id, BOTTOM)
                    }

                    if (row < sideCount - 1) {
                        set.connect(unit.id, BOTTOM, map[row + 1][column].id, TOP)
                    } else {
                        set.connect(unit.id, BOTTOM, deleteBtn.id, TOP)
                        set.connect(deleteBtn.id, TOP, unit.id, BOTTOM)
                        set.connect(memoBtn.id, TOP, unit.id, BOTTOM)
                    }

                    if (column > 0) {
                        set.connect(unit.id, START, map[row][column - 1].id, END)
                    } else {
                        set.connect(unit.id, START, PARENT_ID, START)
                    }

                    if (column < sideCount - 1) {
                        set.connect(unit.id, END, map[row][column + 1].id, START)
                    } else {
                        set.connect(unit.id, END, PARENT_ID, END)
                    }

                    set.constrainedWidth(unit.id, true)
                    set.constrainedHeight(unit.id, true)
                }
            }

            set.applyTo(this)
        }

        private lateinit var preview: TextView

        private fun createPreview() = AppCompatTextView(context).apply {
            id = View.generateViewId()
            textSize = 8f.toPx
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            background = GradientDrawable().apply {
                setStroke(1, borderColorResId)
                setColor(backgroundColorResId)
            }
            setTextColor(numberTextColorResId)

            typeface = ResourcesCompat.getFont(context, R.font.goreyong_ddalgi)
        }

        private fun createNumberButton(row: Int, column: Int, maxRow: Int) =
            createButton().apply {
                layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
                val number = ((row * maxRow) + column + 1)
                tag = number
                text = number.toString()
                setAutoSizeText()
            }

        private fun createButton() = MaterialButton(context).apply {
            id = View.generateViewId()
            insetTop = 0
            insetBottom = 0
            cornerRadius = 0
            minHeight = 0
            maxHeight = 0
            setPadding(0, 0, 0, 0)

            setBackgroundColor(backgroundColorResId)
            rippleColor = ColorStateList.valueOf(numberTextColorResId)
            setTextColor(numberTextColorResId)

            strokeColor = ColorStateList.valueOf(borderColorResId)
            strokeWidth = 3

            typeface = ResourcesCompat.getFont(context, R.font.goreyong_ddalgi)
        }

        /**
         * @author hsbaewa@gmail.com
         * @since 2023/04/12
         * @comment 메모 버튼
         **/
        private fun createMemoButton() = createButton().apply {
            layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
            text = context.getString(R.string.memo_status_on)
            tag = -1
            isSelected = false
            textSize = 8f.toPx
        }

        /**
         * @author hsbaewa@gmail.com
         * @since 2023/04/11
         * @comment 삭제 버튼 생성
         * @return MaterialButton
         **/
        private fun createDeleteButton() = createButton().apply {
            layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
            text = context.getString(R.string.Delete)
            textSize = 8f.toPx
        }

        fun setPreview(text: String) {
            this.preview.text = if (isMemoMode()) {
                context.getString(R.string.memo_format, text)
            } else {
                text
            }
        }

        fun isMemoMode() = findViewWithTag<MaterialButton>(-1)?.isSelected ?: false
    }


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/03/31
     * @comment cell 매트릭스 생성
     * @param rowCount 행 갯수
     * @return cell 매트릭스
     **/
    private fun createCellMatrix(rowCount: Int, disabledMatrix: List<List<Int>>? = null) =
        List(rowCount) { row ->
            List(rowCount) { column ->
                val cell = createCell(row, column).apply {
                    isAccentArea = isAccentArea(row, column, rowValueCount)
                    isEnabled = (disabledMatrix?.getOrNull(row)?.getOrNull(column) ?: 0) == 0
                }

                addView(cell)
                cell
            }
        }

    private fun createCell(row: Int, column: Int) = SudokuCellView(context).apply {
        setLineSpacing(0f, 0.8f)
        letterSpacing = -0.05f
        tag = row to column
        onTouchEvent = {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> onTouchActionDown(it)
                MotionEvent.ACTION_MOVE -> onTouchActionMove(it)
                MotionEvent.ACTION_UP -> onTouchActionUp(it)
            }
        }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/03/31
     * @comment Board Matrix의 아이템
     **/
    private inner class SudokuCellView : MaterialButton {

        constructor(context: Context) : super(context)
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        )

        init {
            insetTop = 0
            insetBottom = 0
            cornerRadius = 0
            minHeight = 0
            maxHeight = 0
            setPadding(0, 0, 0, 0)
            setAutoSizeText()

            setTextColor(numberTextColorResId)

            rippleColor = ColorStateList.valueOf(numberTextColorResId)

            strokeColor = ColorStateList.valueOf(borderColorResId)
            strokeWidth = 1

            isClickable = this@SudokuBoardView.isClickable
            isFocusable = this@SudokuBoardView.isFocusable
            isFocusableInTouchMode = this@SudokuBoardView.isFocusableInTouchMode

            typeface = ResourcesCompat.getFont(context, R.font.goreyong_ddalgi)
        }


        var onTouchEvent: ((event: MotionEvent) -> Unit)? = null


        override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
            val result = super.dispatchTouchEvent(event)
            event.takeIf { it != null && result }
                ?.run { onTouchEvent?.invoke(this) }
            return result
        }

        override fun setEnabled(enabled: Boolean) {
            super.setEnabled(enabled)
            when {
                !enabled -> {
                    if (disabledColorResId == -1) {
                        if (isAccentArea) {
                            setBackgroundColor(accentColorResId)
                        } else {
                            setBackgroundColor(backgroundColorResId)
                        }
                    } else {
                        setBackgroundColor(disabledColorResId)
                    }
                    setTextColor(numberTextDisabledColorResId)
                }

                isAccentArea -> {
                    setBackgroundColor(accentColorResId)
                    setTextColor(numberTextColorResId)
                }

                else -> {
                    setBackgroundColor(backgroundColorResId)
                    setTextColor(numberTextColorResId)
                }
            }
        }

        var isAccentArea = false

    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/03/31
     * @comment 셀 터치 down 이벤트 함수
     * @param event 터치 이벤트에서 수신한 이벤트 객체
     **/
    private fun SudokuCellView.onTouchActionDown(event: MotionEvent) {
        lastTouchDownX = calculatePopupCenterX(event.rawX)
        lastTouchDownY = calculatePopupCenterY(event.rawY)

        cellTouchDownListener?.run {
            val coordinate = getCoordinate()
            if (this(coordinate.first, coordinate.second)) {
                performHapticFeedback()
                numberPopup.show(lastTouchDownX, lastTouchDownY)
            } else {
                lastTouchDownX = 0f
                lastTouchDownY = 0f
            }
        } ?: kotlin.run {
            performHapticFeedback()
            numberPopup.show(lastTouchDownX, lastTouchDownY)
        }
    }

    private fun View.performHapticFeedback() =
        enabledHapticFeedback
            .takeIf { it }
            ?.run {
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }

    private var lastTouchDownX = 0f
    private var lastTouchDownY = 0f


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/03/31
     * @comment 터치를 기준으로 팝업의 center x 좌표 계산, 화면 크기및 boardview를 기준으로 좌, 우 끝부분인 경우 팝업이 밀려서 중간 좌표를 계산해야한다.
     * @param touchX 터치한 x 좌표
     * @return 계산된 결과 X
     **/
    private fun calculatePopupCenterX(touchX: Float): Float {
        // 팝업의 center x 좌표
        val popupCenterX = numberPopup.centerX()
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

    private fun getWindowSize() =
        context.resources.displayMetrics.widthPixels.toFloat() to context.resources.displayMetrics.heightPixels.toFloat()

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/03/31
     * @comment 터치를 기준으로 팝업의 center y 좌표 계산, 화면 크기및 boardview를 기준으로 상, 하 끝부분인 경우 팝업이 밀려서 중간 좌표를 계산해야한다.
     * @param touchY 터치한 y 좌표
     * @return 계산 결과
     **/
    private fun calculatePopupCenterY(touchY: Float): Float {
        val popupCenterY = numberPopup.centerY()
        val windowHeight = getWindowSize().second

        val popupYOffset = when {
            popupCenterY - touchY > 0 -> popupCenterY - touchY
            popupCenterY + touchY > windowHeight -> windowHeight - (popupCenterY + touchY)
            else -> 0F
        }
        return (touchY - popupCenterY) + popupYOffset
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/03/31
     * @comment 셀의 위치 좌표(row, column)
     * @return Pair(row, column)
     **/
    private fun SudokuCellView.getCoordinate() =
        with(tag as Pair<*, *>) { first as Int to second as Int }

    private fun NumberPadPopup.show(centerX: Float, centerY: Float) {
        memoModeOff()
        showAtLocation(rootView, NO_GRAVITY, centerX.roundToInt(), centerY.roundToInt())
    }


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/03/31
     * @comment 터치 후 이동중일 경우 이벤트 함수
     * @param event 터치 이벤트 객체
     **/
    private fun onTouchActionMove(event: MotionEvent) {
        val x = (event.rawX - lastTouchDownX).roundToInt()
        val y = (event.rawY - lastTouchDownY).roundToInt()

        with(numberPopup) {
            children().forEach { numberPadCell ->
                numberPadCell
                    .takeIf { it.getHitRect(hitRect); hitRect.contains(x, y) }
                    ?.let {
                        if (!it.isPressed) {
                            performHapticFeedback()
                            onMoveNumberPad(it)
                        }
                        it.isPressed = true
                    }
                    ?: kotlin.run { numberPadCell.isPressed = false }
            }
        }
    }

    private fun NumberPadPopup.onMoveNumberPad(numberPadCell: View) {
        val number = numberPadCell.tag?.toString() ?: ""
        if (number.toIntOrNull() == -1) {
            if (isMemoMode()) {
                memoModeOff()
            } else {
                memoModeOn()
            }
        } else {
            setPreView(number)
        }
    }

    private val hitRect = Rect()


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/03/31
     * @comment 터치 종료 후 손을 뗏을때 이벤트
     * @param event 이벤트 정보 객체
     **/
    private fun SudokuCellView.onTouchActionUp(event: MotionEvent) {
        val x = (event.rawX - lastTouchDownX).roundToInt()
        val y = (event.rawY - lastTouchDownY).roundToInt()

        // 터치 up 했을때의 pad의 숫자를 cell 에 표기
        numberPopup
            .children()
            .find { it.getHitRect(hitRect); hitRect.contains(x, y) }
            ?.let { numberPadCell ->
                val selectedNumber = (numberPadCell.tag as? Int)?.takeIf { it >= 0 }
                if (numberPopup.isMemoMode()) {
                    if (!isInitMemo()) {
                        cellValueChangedListener?.run {
                            val cellCoordinate = getCoordinate()
                            this(cellCoordinate.first, cellCoordinate.second, null)
                        }
                        initMemo()
                    }
                    selectedNumber?.run { toggleNumberInMemo(this) }
                } else {
                    clearMemo()
                    cellValueChangedListener?.run {
                        val cellCoordinate = getCoordinate()
                        if (this(cellCoordinate.first, cellCoordinate.second, selectedNumber)) {
                            text = selectedNumber?.toString() ?: ""
                        }
                    } ?: run { text = selectedNumber?.toString() ?: "" }
                }
                performClick()
            }

        lastTouchDownX = 0f
        lastTouchDownY = 0f
        numberPopup.dismiss()
    }

    private fun SudokuCellView.isInitMemo() = !isAutoSizeText()
    private fun SudokuCellView.initMemo() {
        setAutoSizeTextNone()
        typeface = null
        textSize = measuredHeight / 9f
        text = ""
    }

    private fun SudokuCellView.getMemoList() =
        text.split(" ", "\n")
            .mapNotNull { it.toIntOrNull() }
            .toMutableList()

    private fun SudokuCellView.toggleNumberInMemo(number: Int) =
        getMemoList()
            .also {
                if (it.contains(number)) {
                    it.remove(number)
                } else {
                    it.add(number)
                }
            }
            .let {
                text = buildString {
                    it.sort()
                    it.forEachIndexed { index, i ->
                        if (index > 0) {
                            if (index % sqrt(rowValueCount.toDouble()).toInt() == 0) {
                                append("\n")
                            } else {
                                append(" ")
                            }
                        }
                        append(i)
                    }
                }
            }

    private fun SudokuCellView.clearMemo() {
        setAutoSizeText()
        val typeFace = ResourcesCompat.getFont(context, R.font.goreyong_ddalgi)
        typeface = typeFace
        text = ""
    }

    private fun isAccentArea(row: Int, column: Int, rowCount: Int): Boolean {
        val rowConditionFilter = row / sqrt(rowCount.toDouble()).toInt()
        val columnConditionFilter = column / sqrt(rowCount.toDouble()).toInt()
        return (rowConditionFilter + columnConditionFilter) % 2 == 0
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val sizeOfWidth = getSize(widthMeasureSpec)
        val modeOfWidth = getMode(widthMeasureSpec)
        val sizeOfHeight = getSize(heightMeasureSpec)
        val modeOfHeight = getMode(heightMeasureSpec)

        val sizeSpec = when {
            sizeOfWidth < sizeOfHeight -> makeMeasureSpec(sizeOfWidth, modeOfWidth)
            else -> makeMeasureSpec(sizeOfHeight, modeOfHeight)
        }

        val size = getSize(sizeSpec)
        val mode = getMode(sizeSpec)
        log("onMeasure(board Size = $size)")

        rowValueCount.takeIf { it > 0 }?.run {
            val cellSize = calculateCellSize(size)
            log("onMeasure(cell Size = $cellSize)")

            children.forEach {
                val cellSizeSpec = makeMeasureSpec(cellSize, mode)
                it.measure(cellSizeSpec, cellSizeSpec)
            }
        }

        setMeasuredDimension(sizeSpec, sizeSpec)
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/12
     * @comment 구분선 너비
     * @return 구분선 너비
     **/
    private fun getBorderWidth() = BORDER_WIDTH

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/12
     * @comment 구분선 갯수
     * @return 구분선 갯수
     **/
    private fun getBorderCount() = rowValueCount.takeIf { it > 0 }?.run { this + 1 } ?: 0

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/12
     * @comment box Border를 위한 추가 width
     * @return border
     **/
    private fun getAdditionalBoxBorderWidth() =
        (BOX_BORDER_WIDTH - BORDER_WIDTH).takeIf { it > 0 } ?: 0

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/12
     * @comment 박스 구분선 갯수(외곽 제외)
     * @return 박스 구분선 갯수(외곽 제외)
     **/
    private fun getBoxBorderCount() = rowValueCount.takeIf { it > 0 }
        ?.run { sqrt(this.toDouble()).toInt() - 1 }
        ?: 0

    private fun calculateCellSize(backgroundSize: Int) =
        rowValueCount.takeIf { it > 0 }
            ?.run {
                val borderWidth = getBorderWidth() // 구분선 너비
                val borderCount = getBorderCount() // 구분선 갯수

                val boxBorderWidth = getAdditionalBoxBorderWidth() // 박스 구분선 추가 너비
                val boxBorderCount = getBoxBorderCount() // 박스 구분선 갯수(외곽 제외)

                // 보더 사이즈 총합
                val allBorderSize = (borderWidth * borderCount) + (boxBorderWidth * boxBorderCount)

                log("onMeasure(borderWidth:$borderWidth, borderCount:$borderCount, boxBorderWidth:$boxBorderWidth, boxBorderCount:$boxBorderCount)")
                // 소수점 이하 짜름(integer 형태여야 함)
                (backgroundSize - allBorderSize) / this
            } ?: 0

    //
    fun setCellValue(row: Int, column: Int, value: Int) =
        findCellWithCoordinate(row, column)?.run {
            text = value.takeIf { it > 0 }?.run { toString() }
        }

    private fun findCellWithCoordinate(row: Int, column: Int) =
        findViewWithTag<SudokuCellView>(row to column)

    fun setError(row: Int, column: Int, isError: Boolean) =
        findCellWithCoordinate(row, column)?.run {
            setTextColor(
                if (isError) {
                    numberTextErrorColorResId
                } else {
                    if (isEnabled) {
                        numberTextColorResId
                    } else {
                        numberTextDisabledColorResId
                    }
                }
            )
        }


    var cellTouchDownListener: ((row: Int, column: Int) -> Boolean)? = null
    var cellValueChangedListener: ((row: Int, column: Int, value: Int?) -> Boolean)? = null

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        val layoutWidth = p3 - p1
        val layoutHeight = p4 - p2
        log("onLayout(layoutWidth:$layoutWidth, layoutHeight:$layoutHeight)")
        rowValueCount.takeIf { it > 0 }?.run {
            val borderWidth = getBorderWidth() // 구분선 너비
            val borderCount = getBorderCount() // 구분선 갯수

            val boxBorderWidth = getAdditionalBoxBorderWidth() // 박스 구분선 추가 너비
            val boxBorderCount = getBoxBorderCount() // 박스 구분선 갯수(외곽 제외)

            // 보더 사이즈 총합
            val allBorderSize = (borderWidth * borderCount) + (boxBorderWidth * boxBorderCount)

            // 소수점 이하 짜름(integer 형태여야 함)
            val cellWidth = (layoutWidth - allBorderSize) / this
            val cellHeight = (layoutHeight - allBorderSize) / this
            log("onLayout(cellWidth:$cellWidth, cellHeight:$cellHeight)")

            // 보더 와 셀 사이즈를 제외한 나머지 사이즈를 outlineWidth로 한다.
            val remainWidth = layoutWidth - (cellWidth * this) - allBorderSize
            val remainHeight = layoutHeight - (cellHeight * this) - allBorderSize

            val outlineWidth = remainWidth / 2 // 보드 외곽
            val outlineHeight = remainHeight / 2

            log("onLayout(outlineWidth:$outlineWidth, outlineHeight:$outlineHeight)")

            children.forEach {
                val cellViewCoordinate = (it as SudokuCellView).getCoordinate()
                val column = cellViewCoordinate.second
                val row = cellViewCoordinate.first

                var start = outlineWidth + getBorderWidth()  // 맨 왼쪽 Border 사이즈
                start += column * cellWidth // column 에 따라 증가
                start += column * getBorderWidth() // 구분선
                start += (column / sqrt(this.toDouble())).toInt() * getAdditionalBoxBorderWidth()

                val end = start + cellWidth

                var top = outlineHeight + getBorderWidth()  // 맨 왼쪽 Border 사이즈
                top += row * cellHeight // column 에 따라 증가
                top += row * getBorderWidth() // 구분선
                top += (row / sqrt(this.toDouble())).toInt() * getAdditionalBoxBorderWidth()

                val bottom = top + cellHeight

                it.layout(start, top, end, bottom)
            }
        }
    }

    var enabledHapticFeedback = true
}