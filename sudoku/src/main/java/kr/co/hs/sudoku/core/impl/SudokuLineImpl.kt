package kr.co.hs.sudoku.core.impl

import kr.co.hs.sudoku.core.CellEntity
import kr.co.hs.sudoku.core.SudokuLine
import kr.co.hs.sudoku.core.SudokuStrategyRule


class SudokuLineImpl(
    count: Int,
    private val list: List<CellEntity<Int>>,
    private val strategyRule: SudokuStrategyRule = SudokuStrategyRuleImpl()
) : SudokuLine,
    SudokuCellListImpl(count, list),
    SudokuStrategyRule by strategyRule {

    constructor(
        count: Int
    ) : this(count, List(count) { IntCellEntityImpl() }) {
        setCellCollection(this)
    }

    override fun getAvailableValueInLine(): List<Int> {
        val result = MutableList(count) { it + 1 }
        toValueList().forEach { result.remove(it) }
        return result
    }
}