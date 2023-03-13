package kr.co.hs.sudoku.model

interface MutableCellTable<ValueType> :
    MutableCellCollection<ValueType>,
    CellTable<ValueType> {
    fun setCell(row: Int, column: Int, cell: IntCoordinateCellEntity)
    fun setCell(coordinate: Coordinate<Int>, cell: IntCoordinateCellEntity)
    fun setValue(row: Int, column: Int, value: ValueType)
    fun setValue(coordinate: Coordinate<Int>, value: ValueType)
}