package kr.co.hs.sudoku.model

interface MutableCellCollection<ValueType> : CellCollection<ValueType> {
    fun clear()
    fun clear(value: ValueType)
}