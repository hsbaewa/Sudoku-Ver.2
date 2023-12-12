package kr.co.hs.sudoku.model.stage.impl

import kr.co.hs.sudoku.model.stage.CellCollection
import kr.co.hs.sudoku.model.stage.CellList
import kr.co.hs.sudoku.model.stage.CellValueEntity
import kr.co.hs.sudoku.model.stage.SudokuStrategyRule

class SudokuStrategyRuleImpl : SudokuStrategyRule {

    private var collection: CellCollection<Int>? = null

    override fun setCellCollection(collection: CellCollection<Int>) {
        this.collection = collection
    }

    override fun getDuplicatedCells(): CellList<Int> {
        return collection?.run {
            val result = toList()
                .filter { it.value !is CellValueEntity.Empty }
                .groupBy { it.getValue() }
                .filterValues { it.size > 1 }
                .flatMap { it.value }
            SudokuCellListImpl(result.size, result)
        } ?: SudokuCellListImpl(0, emptyList())
    }

    override fun getDuplicatedCellCount() = getDuplicatedCells().count

    override fun getEmptyCells(): CellList<Int> {
        return collection?.run {
            val result = toList()
                .filter { it.value is CellValueEntity.Empty }
            SudokuCellListImpl(result.size, result)
        } ?: SudokuCellListImpl(0, emptyList())
    }

    override fun getEmptyCellCount() = getEmptyCells().count

    override fun isSudokuClear() =
        getDuplicatedCells().isEmpty() && getEmptyCells().isEmpty()
}