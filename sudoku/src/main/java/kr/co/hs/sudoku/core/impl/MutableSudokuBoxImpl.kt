package kr.co.hs.sudoku.core.impl

import kr.co.hs.sudoku.core.IntCoordinateCellEntity
import kr.co.hs.sudoku.core.MutableSudokuBox
import kr.co.hs.sudoku.core.SudokuStrategyRule


class MutableSudokuBoxImpl(
    override val rowCount: Int,
    override val columnCount: Int,
    private val table: List<MutableList<IntCoordinateCellEntity>>
) : MutableSudokuBox,
    MutableSudokuCellTableImpl(rowCount, columnCount, table),
    SudokuStrategyRule by SudokuStrategyRuleImpl() {

    constructor(
        rowCount: Int,
        columnCount: Int
    ) : this(
        rowCount,
        columnCount,
        List(rowCount) { row ->
            MutableList(columnCount) { column ->
                IntCoordinateCellEntityImpl(row, column)
            }
        }
    ) {
        setCellCollection(this)
    }

    override fun getAvailableValueInBox(): List<Int> {
        val result = MutableList(rowCount * columnCount) { it + 1 }
        toValueList().forEach { result.remove(it) }
        return result
    }
}