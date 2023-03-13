package kr.co.hs.sudoku.model.stage

interface MutableCellCollection<ValueType> : CellCollection<ValueType> {
    fun clear()
    fun clear(value: ValueType)
}