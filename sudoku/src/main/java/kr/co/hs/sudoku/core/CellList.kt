package kr.co.hs.sudoku.core

interface CellList<ValueType> : CellCollection<ValueType> {
    fun getCell(idx: Int): CellEntity<ValueType>
    fun getValue(idx: Int): ValueType
}