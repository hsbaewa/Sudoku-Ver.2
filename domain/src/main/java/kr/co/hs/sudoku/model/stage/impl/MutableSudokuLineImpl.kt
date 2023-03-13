package kr.co.hs.sudoku.model.stage.impl

import kr.co.hs.sudoku.model.stage.CellEntity
import kr.co.hs.sudoku.model.stage.MutableSudokuLine
import kr.co.hs.sudoku.model.stage.SudokuStrategyRule

class MutableSudokuLineImpl(
    count: Int,
    private val list: MutableList<CellEntity<Int>>,
    private val strategyRule: SudokuStrategyRule = SudokuStrategyRuleImpl()
) : MutableSudokuLine,
    MutableSudokuCellListImpl(count, list),
    SudokuStrategyRule by strategyRule {

    constructor(
        count: Int
    ) : this(
        count,
        MutableList(count) { IntCellEntityImpl() }
    ) {
        setCellCollection(this)
    }

    override fun getAvailableValueInLine(): List<Int> {
        val result = MutableList(count) { it + 1 }
        toValueList().forEach { result.remove(it) }
        return result
    }
}