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
            inflate(context, R.layout.layout_matrix_beginner_item_view, this)
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

        override val matrixCell: List<List<View>> by lazy {
            listOf(
                listOf(
                    findViewById(R.id.cell_0_0),
                    findViewById(R.id.cell_0_1),
                    findViewById(R.id.cell_0_2),
                    findViewById(R.id.cell_0_3),
                    findViewById(R.id.cell_0_4),
                    findViewById(R.id.cell_0_5),
                    findViewById(R.id.cell_0_6),
                    findViewById(R.id.cell_0_7),
                    findViewById(R.id.cell_0_8)
                ),
                listOf(
                    findViewById(R.id.cell_1_0),
                    findViewById(R.id.cell_1_1),
                    findViewById(R.id.cell_1_2),
                    findViewById(R.id.cell_1_3),
                    findViewById(R.id.cell_1_4),
                    findViewById(R.id.cell_1_5),
                    findViewById(R.id.cell_1_6),
                    findViewById(R.id.cell_1_7),
                    findViewById(R.id.cell_1_8)
                ),
                listOf(
                    findViewById(R.id.cell_2_0),
                    findViewById(R.id.cell_2_1),
                    findViewById(R.id.cell_2_2),
                    findViewById(R.id.cell_2_3),
                    findViewById(R.id.cell_2_4),
                    findViewById(R.id.cell_2_5),
                    findViewById(R.id.cell_2_6),
                    findViewById(R.id.cell_2_7),
                    findViewById(R.id.cell_2_8)
                ),
                listOf(
                    findViewById(R.id.cell_3_0),
                    findViewById(R.id.cell_3_1),
                    findViewById(R.id.cell_3_2),
                    findViewById(R.id.cell_3_3),
                    findViewById(R.id.cell_3_4),
                    findViewById(R.id.cell_3_5),
                    findViewById(R.id.cell_3_6),
                    findViewById(R.id.cell_3_7),
                    findViewById(R.id.cell_3_8)
                ),
                listOf(
                    findViewById(R.id.cell_4_0),
                    findViewById(R.id.cell_4_1),
                    findViewById(R.id.cell_4_2),
                    findViewById(R.id.cell_4_3),
                    findViewById(R.id.cell_4_4),
                    findViewById(R.id.cell_4_5),
                    findViewById(R.id.cell_4_6),
                    findViewById(R.id.cell_4_7),
                    findViewById(R.id.cell_4_8)
                ),
                listOf(
                    findViewById(R.id.cell_5_0),
                    findViewById(R.id.cell_5_1),
                    findViewById(R.id.cell_5_2),
                    findViewById(R.id.cell_5_3),
                    findViewById(R.id.cell_5_4),
                    findViewById(R.id.cell_5_5),
                    findViewById(R.id.cell_5_6),
                    findViewById(R.id.cell_5_7),
                    findViewById(R.id.cell_5_8)
                ),
                listOf(
                    findViewById(R.id.cell_6_0),
                    findViewById(R.id.cell_6_1),
                    findViewById(R.id.cell_6_2),
                    findViewById(R.id.cell_6_3),
                    findViewById(R.id.cell_6_4),
                    findViewById(R.id.cell_6_5),
                    findViewById(R.id.cell_6_6),
                    findViewById(R.id.cell_6_7),
                    findViewById(R.id.cell_6_8)
                ),
                listOf(
                    findViewById(R.id.cell_7_0),
                    findViewById(R.id.cell_7_1),
                    findViewById(R.id.cell_7_2),
                    findViewById(R.id.cell_7_3),
                    findViewById(R.id.cell_7_4),
                    findViewById(R.id.cell_7_5),
                    findViewById(R.id.cell_7_6),
                    findViewById(R.id.cell_7_7),
                    findViewById(R.id.cell_7_8)
                ),
                listOf(
                    findViewById(R.id.cell_8_0),
                    findViewById(R.id.cell_8_1),
                    findViewById(R.id.cell_8_2),
                    findViewById(R.id.cell_8_3),
                    findViewById(R.id.cell_8_4),
                    findViewById(R.id.cell_8_5),
                    findViewById(R.id.cell_8_6),
                    findViewById(R.id.cell_8_7),
                    findViewById(R.id.cell_8_8)
                )
            )
        }

        init {
            inflate(context, R.layout.layout_matrix_intermediate_item_view, this)
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

        override val matrixCell: List<List<View>> by lazy {
            listOf(
                listOf(
                    findViewById(R.id.cell_0_0),
                    findViewById(R.id.cell_0_1),
                    findViewById(R.id.cell_0_2),
                    findViewById(R.id.cell_0_3),
                    findViewById(R.id.cell_0_4),
                    findViewById(R.id.cell_0_5),
                    findViewById(R.id.cell_0_6),
                    findViewById(R.id.cell_0_7),
                    findViewById(R.id.cell_0_8),
                    findViewById(R.id.cell_0_9),
                    findViewById(R.id.cell_0_10),
                    findViewById(R.id.cell_0_11),
                    findViewById(R.id.cell_0_12),
                    findViewById(R.id.cell_0_13),
                    findViewById(R.id.cell_0_14),
                    findViewById(R.id.cell_0_15)
                ),
                listOf(
                    findViewById(R.id.cell_1_0),
                    findViewById(R.id.cell_1_1),
                    findViewById(R.id.cell_1_2),
                    findViewById(R.id.cell_1_3),
                    findViewById(R.id.cell_1_4),
                    findViewById(R.id.cell_1_5),
                    findViewById(R.id.cell_1_6),
                    findViewById(R.id.cell_1_7),
                    findViewById(R.id.cell_1_8),
                    findViewById(R.id.cell_1_9),
                    findViewById(R.id.cell_1_10),
                    findViewById(R.id.cell_1_11),
                    findViewById(R.id.cell_1_12),
                    findViewById(R.id.cell_1_13),
                    findViewById(R.id.cell_1_14),
                    findViewById(R.id.cell_1_15)
                ),
                listOf(
                    findViewById(R.id.cell_2_0),
                    findViewById(R.id.cell_2_1),
                    findViewById(R.id.cell_2_2),
                    findViewById(R.id.cell_2_3),
                    findViewById(R.id.cell_2_4),
                    findViewById(R.id.cell_2_5),
                    findViewById(R.id.cell_2_6),
                    findViewById(R.id.cell_2_7),
                    findViewById(R.id.cell_2_8),
                    findViewById(R.id.cell_2_9),
                    findViewById(R.id.cell_2_10),
                    findViewById(R.id.cell_2_11),
                    findViewById(R.id.cell_2_12),
                    findViewById(R.id.cell_2_13),
                    findViewById(R.id.cell_2_14),
                    findViewById(R.id.cell_2_15)
                ),
                listOf(
                    findViewById(R.id.cell_3_0),
                    findViewById(R.id.cell_3_1),
                    findViewById(R.id.cell_3_2),
                    findViewById(R.id.cell_3_3),
                    findViewById(R.id.cell_3_4),
                    findViewById(R.id.cell_3_5),
                    findViewById(R.id.cell_3_6),
                    findViewById(R.id.cell_3_7),
                    findViewById(R.id.cell_3_8),
                    findViewById(R.id.cell_3_9),
                    findViewById(R.id.cell_3_10),
                    findViewById(R.id.cell_3_11),
                    findViewById(R.id.cell_3_12),
                    findViewById(R.id.cell_3_13),
                    findViewById(R.id.cell_3_14),
                    findViewById(R.id.cell_3_15)
                ),
                listOf(
                    findViewById(R.id.cell_4_0),
                    findViewById(R.id.cell_4_1),
                    findViewById(R.id.cell_4_2),
                    findViewById(R.id.cell_4_3),
                    findViewById(R.id.cell_4_4),
                    findViewById(R.id.cell_4_5),
                    findViewById(R.id.cell_4_6),
                    findViewById(R.id.cell_4_7),
                    findViewById(R.id.cell_4_8),
                    findViewById(R.id.cell_4_9),
                    findViewById(R.id.cell_4_10),
                    findViewById(R.id.cell_4_11),
                    findViewById(R.id.cell_4_12),
                    findViewById(R.id.cell_4_13),
                    findViewById(R.id.cell_4_14),
                    findViewById(R.id.cell_4_15)
                ),
                listOf(
                    findViewById(R.id.cell_5_0),
                    findViewById(R.id.cell_5_1),
                    findViewById(R.id.cell_5_2),
                    findViewById(R.id.cell_5_3),
                    findViewById(R.id.cell_5_4),
                    findViewById(R.id.cell_5_5),
                    findViewById(R.id.cell_5_6),
                    findViewById(R.id.cell_5_7),
                    findViewById(R.id.cell_5_8),
                    findViewById(R.id.cell_5_9),
                    findViewById(R.id.cell_5_10),
                    findViewById(R.id.cell_5_11),
                    findViewById(R.id.cell_5_12),
                    findViewById(R.id.cell_5_13),
                    findViewById(R.id.cell_5_14),
                    findViewById(R.id.cell_5_15)
                ),
                listOf(
                    findViewById(R.id.cell_6_0),
                    findViewById(R.id.cell_6_1),
                    findViewById(R.id.cell_6_2),
                    findViewById(R.id.cell_6_3),
                    findViewById(R.id.cell_6_4),
                    findViewById(R.id.cell_6_5),
                    findViewById(R.id.cell_6_6),
                    findViewById(R.id.cell_6_7),
                    findViewById(R.id.cell_6_8),
                    findViewById(R.id.cell_6_9),
                    findViewById(R.id.cell_6_10),
                    findViewById(R.id.cell_6_11),
                    findViewById(R.id.cell_6_12),
                    findViewById(R.id.cell_6_13),
                    findViewById(R.id.cell_6_14),
                    findViewById(R.id.cell_6_15)
                ),
                listOf(
                    findViewById(R.id.cell_7_0),
                    findViewById(R.id.cell_7_1),
                    findViewById(R.id.cell_7_2),
                    findViewById(R.id.cell_7_3),
                    findViewById(R.id.cell_7_4),
                    findViewById(R.id.cell_7_5),
                    findViewById(R.id.cell_7_6),
                    findViewById(R.id.cell_7_7),
                    findViewById(R.id.cell_7_8),
                    findViewById(R.id.cell_7_9),
                    findViewById(R.id.cell_7_10),
                    findViewById(R.id.cell_7_11),
                    findViewById(R.id.cell_7_12),
                    findViewById(R.id.cell_7_13),
                    findViewById(R.id.cell_7_14),
                    findViewById(R.id.cell_7_15)
                ),
                listOf(
                    findViewById(R.id.cell_8_0),
                    findViewById(R.id.cell_8_1),
                    findViewById(R.id.cell_8_2),
                    findViewById(R.id.cell_8_3),
                    findViewById(R.id.cell_8_4),
                    findViewById(R.id.cell_8_5),
                    findViewById(R.id.cell_8_6),
                    findViewById(R.id.cell_8_7),
                    findViewById(R.id.cell_8_8),
                    findViewById(R.id.cell_8_9),
                    findViewById(R.id.cell_8_10),
                    findViewById(R.id.cell_8_11),
                    findViewById(R.id.cell_8_12),
                    findViewById(R.id.cell_8_13),
                    findViewById(R.id.cell_8_14),
                    findViewById(R.id.cell_8_15)
                ),
                listOf(
                    findViewById(R.id.cell_9_0),
                    findViewById(R.id.cell_9_1),
                    findViewById(R.id.cell_9_2),
                    findViewById(R.id.cell_9_3),
                    findViewById(R.id.cell_9_4),
                    findViewById(R.id.cell_9_5),
                    findViewById(R.id.cell_9_6),
                    findViewById(R.id.cell_9_7),
                    findViewById(R.id.cell_9_8),
                    findViewById(R.id.cell_9_9),
                    findViewById(R.id.cell_9_10),
                    findViewById(R.id.cell_9_11),
                    findViewById(R.id.cell_9_12),
                    findViewById(R.id.cell_9_13),
                    findViewById(R.id.cell_9_14),
                    findViewById(R.id.cell_9_15)
                ),
                listOf(
                    findViewById(R.id.cell_10_0),
                    findViewById(R.id.cell_10_1),
                    findViewById(R.id.cell_10_2),
                    findViewById(R.id.cell_10_3),
                    findViewById(R.id.cell_10_4),
                    findViewById(R.id.cell_10_5),
                    findViewById(R.id.cell_10_6),
                    findViewById(R.id.cell_10_7),
                    findViewById(R.id.cell_10_8),
                    findViewById(R.id.cell_10_9),
                    findViewById(R.id.cell_10_10),
                    findViewById(R.id.cell_10_11),
                    findViewById(R.id.cell_10_12),
                    findViewById(R.id.cell_10_13),
                    findViewById(R.id.cell_10_14),
                    findViewById(R.id.cell_10_15)
                ),
                listOf(
                    findViewById(R.id.cell_11_0),
                    findViewById(R.id.cell_11_1),
                    findViewById(R.id.cell_11_2),
                    findViewById(R.id.cell_11_3),
                    findViewById(R.id.cell_11_4),
                    findViewById(R.id.cell_11_5),
                    findViewById(R.id.cell_11_6),
                    findViewById(R.id.cell_11_7),
                    findViewById(R.id.cell_11_8),
                    findViewById(R.id.cell_11_9),
                    findViewById(R.id.cell_11_10),
                    findViewById(R.id.cell_11_11),
                    findViewById(R.id.cell_11_12),
                    findViewById(R.id.cell_11_13),
                    findViewById(R.id.cell_11_14),
                    findViewById(R.id.cell_11_15)
                ),
                listOf(
                    findViewById(R.id.cell_12_0),
                    findViewById(R.id.cell_12_1),
                    findViewById(R.id.cell_12_2),
                    findViewById(R.id.cell_12_3),
                    findViewById(R.id.cell_12_4),
                    findViewById(R.id.cell_12_5),
                    findViewById(R.id.cell_12_6),
                    findViewById(R.id.cell_12_7),
                    findViewById(R.id.cell_12_8),
                    findViewById(R.id.cell_12_9),
                    findViewById(R.id.cell_12_10),
                    findViewById(R.id.cell_12_11),
                    findViewById(R.id.cell_12_12),
                    findViewById(R.id.cell_12_13),
                    findViewById(R.id.cell_12_14),
                    findViewById(R.id.cell_12_15)
                ),
                listOf(
                    findViewById(R.id.cell_13_0),
                    findViewById(R.id.cell_13_1),
                    findViewById(R.id.cell_13_2),
                    findViewById(R.id.cell_13_3),
                    findViewById(R.id.cell_13_4),
                    findViewById(R.id.cell_13_5),
                    findViewById(R.id.cell_13_6),
                    findViewById(R.id.cell_13_7),
                    findViewById(R.id.cell_13_8),
                    findViewById(R.id.cell_13_9),
                    findViewById(R.id.cell_13_10),
                    findViewById(R.id.cell_13_11),
                    findViewById(R.id.cell_13_12),
                    findViewById(R.id.cell_13_13),
                    findViewById(R.id.cell_13_14),
                    findViewById(R.id.cell_13_15)
                ),
                listOf(
                    findViewById(R.id.cell_14_0),
                    findViewById(R.id.cell_14_1),
                    findViewById(R.id.cell_14_2),
                    findViewById(R.id.cell_14_3),
                    findViewById(R.id.cell_14_4),
                    findViewById(R.id.cell_14_5),
                    findViewById(R.id.cell_14_6),
                    findViewById(R.id.cell_14_7),
                    findViewById(R.id.cell_14_8),
                    findViewById(R.id.cell_14_9),
                    findViewById(R.id.cell_14_10),
                    findViewById(R.id.cell_14_11),
                    findViewById(R.id.cell_14_12),
                    findViewById(R.id.cell_14_13),
                    findViewById(R.id.cell_14_14),
                    findViewById(R.id.cell_14_15)
                ),
                listOf(
                    findViewById(R.id.cell_15_0),
                    findViewById(R.id.cell_15_1),
                    findViewById(R.id.cell_15_2),
                    findViewById(R.id.cell_15_3),
                    findViewById(R.id.cell_15_4),
                    findViewById(R.id.cell_15_5),
                    findViewById(R.id.cell_15_6),
                    findViewById(R.id.cell_15_7),
                    findViewById(R.id.cell_15_8),
                    findViewById(R.id.cell_15_9),
                    findViewById(R.id.cell_15_10),
                    findViewById(R.id.cell_15_11),
                    findViewById(R.id.cell_15_12),
                    findViewById(R.id.cell_15_13),
                    findViewById(R.id.cell_15_14),
                    findViewById(R.id.cell_15_15)
                )
            )
        }

        init {
            inflate(context, R.layout.layout_matrix_advanced_item_view, this)
            setBackgroundColor(borderColor)
        }
    }
}