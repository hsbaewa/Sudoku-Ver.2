package kr.co.hs.sudoku.core

interface MutableCellCollection<ValueType> : CellCollection<ValueType> {
    fun clear()
    fun clear(value: ValueType)
}