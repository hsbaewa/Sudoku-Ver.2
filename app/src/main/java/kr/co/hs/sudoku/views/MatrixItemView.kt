package kr.co.hs.sudoku.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.extension.platform.ContextExtension.getColorCompat

sealed class MatrixItemView : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @Suppress("unused")
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)


    private val openColor: Int by lazy { context.getColorCompat(R.color.white) }
    private val closedColor: Int by lazy { context.getColorCompat(R.color.gray_400) }
    protected val borderColor: Int by lazy { context.getColorCompat(R.color.gray_600) }

    protected abstract val matrixCell: List<List<View>>

    fun setMatrix(matrix: List<List<Int>>) {
        matrix.forEachIndexed { row, ints ->
            ints.forEachIndexed { column, i ->
                matrixCell[row][column].setBackgroundColor(if (i > 0) closedColor else openColor)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 정사각형
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    class BeginnerMatrixItemView : MatrixItemView {
        constructor(context: Context) : super(context)
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        )

        @Suppress("unused")
        constructor(
            context: Context,
            attrs: AttributeSet?,
            defStyleAttr: Int,
            defStyleRes: Int
        ) : super(context, attrs, defStyleAttr, defStyleRes)

        override val matrixCell: List<List<View>> by lazy {
            listOf(
                listOf(
                    findViewById(R.id.cell_0_0),
                    findViewById(R.id.cell_0_1),
                    findViewById(R.id.cell_0_2),
                    findViewById(R.id.cell_0_3)
                ),
                listOf(
                    findViewById(R.id.cell_1_0),
                    findViewById(R.id.cell_1_1),
                    findViewById(R.id.cell_1_2),
                    findViewById(R.id.cell_1_3)
                ),
                listOf(
                    findViewById(R.id.cell_2_0),
                    findViewById(R.id.cell_2_1),
                    findViewById(R.id.cell_2_2),
                    findViewById(R.id.cell_2_3)
                ),
                listOf(
                    findViewById(R.id.cell_3_0),
                    findViewById(R.id.cell_3_1),
                    findViewById(R.id.cell_3_2),
                    findViewById(R.id.cell_3_3)
                )
            )
        }

        init {
            inflate(context, R.layout.layout_matrix_easy_item_view, this)
            setBackgroundColor(borderColor)
        }
    }


    class IntermediateMatrixItemView : MatrixItemView {
        constructor(context: Context) : super(context)
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        )

        @Suppress("unused")
        constructor(
            context: Context,
            attrs: AttributeSet?,
            defStyleAttr: Int,
            defStyleRes: Int
        ) : super(context, attrs, defStyleAttr, defStyleRes)

        override val matrixCell: List<List<View>>
            get() = TODO("Not yet implemented")

        init {
            inflate(context, R.layout.layout_matrix_easy_item_view, this)
            setBackgroundColor(borderColor)
        }
    }

    class AdvancedMatrixItemView : MatrixItemView {
        constructor(context: Context) : super(context)
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        )

        @Suppress("unused")
        constructor(
            context: Context,
            attrs: AttributeSet?,
            defStyleAttr: Int,
            defStyleRes: Int
        ) : super(context, attrs, defStyleAttr, defStyleRes)

        override val matrixCell: List<List<View>>
            get() = TODO("Not yet implemented")

        init {
            inflate(context, R.layout.layout_matrix_easy_item_view, this)
            setBackgroundColor(borderColor)
        }
    }
}