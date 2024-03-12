package kr.co.hs.sudoku.core.impl

import kr.co.hs.sudoku.core.IntCoordinateCellEntity
import kr.co.hs.sudoku.core.SudokuBox
import kr.co.hs.sudoku.core.SudokuStrategyRule

class SudokuBoxImpl(
    rowCount: Int,
    columnCount: Int,
    private val table: List<List<IntCoordinateCellEntity>>,
    private val strategyRule: SudokuStrategyRule = SudokuStrategyRuleImpl()
) : SudokuBox,
    SudokuCellTableImpl(rowCount, columnCount, table),
    SudokuStrategyRule by strategyRule {

    constructor(
        rowCount: Int,
        columnCount: Int
    ) : this(rowCount, columnCount,
        List(rowCount) { row ->
            List(columnCount) { column ->
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