package kr.co.hs.sudoku.model.stage.impl

import kr.co.hs.sudoku.model.stage.CellEntity
import kr.co.hs.sudoku.model.stage.CellList

open class SudokuCellListImpl(
    count: Int,
    private val list: List<CellEntity<Int>>
) : CellCollectionImpl<Int>(count),
    CellList<Int> {

    constructor(
        count: Int,
    ) : this(count, List(count) { IntCellEntityImpl() })

    /**
     * CellCollection
     */
    override fun toList() = list


    /**
     * CellList
     */
    override fun getCell(idx: Int) = list[idx]
    override fun getValue(idx: Int) = getCell(idx).getValue()
}