package kr.co.hs.sudoku.model

interface MutableCellList<ValueType> :
    MutableCellCollection<ValueType>,
    CellList<ValueType> {
    fun setCell(idx: Int, cell: CellEntity<ValueType>)
    fun setValue(idx: Int, value: ValueType)
}