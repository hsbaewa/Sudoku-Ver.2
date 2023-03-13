package kr.co.hs.sudoku.model

interface CellList<ValueType> : CellCollection<ValueType> {
    fun getCell(idx: Int): CellEntity<ValueType>
    fun getValue(idx: Int): ValueType
}