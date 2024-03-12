package kr.co.hs.sudoku.core.impl

import kr.co.hs.sudoku.core.CellEntity
import kr.co.hs.sudoku.core.MutableCellList

open class MutableSudokuCellListImpl(
    count: Int,
    private val list: MutableList<CellEntity<Int>>
) : MutableCellList<Int>,
    SudokuCellListImpl(count, list) {

    constructor(
        count: Int
    ) : this(count, MutableList(count) { IntCellEntityImpl() })

    override fun setCell(idx: Int, cell: CellEntity<Int>) {
        list[idx] = cell
    }

    override fun setValue(idx: Int, value: Int) =
        list[idx].run {
            if (isMutable()) {
                setValue(value)
            } else {
                toMutable(value)
            }
        }

    override fun clear() =
        list.filter { it.isMutable() }
            .forEach { it.toEmpty() }

    override fun clear(value: Int) =
        list.filter { it.isMutable() && it.getValue() == value }
            .forEach { it.toEmpty() }

}