package kr.co.hs.sudoku.model.stage

interface CellList<ValueType> : CellCollection<ValueType> {
    fun getCell(idx: Int): CellEntity<ValueType>
    fun getValue(idx: Int): ValueType
}