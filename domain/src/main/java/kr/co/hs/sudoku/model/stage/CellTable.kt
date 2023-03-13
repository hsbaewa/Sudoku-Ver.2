package kr.co.hs.sudoku.model.stage

interface CellTable<ValueType> : CellCollection<ValueType> {
    val rowCount: Int
    val columnCount: Int
    fun getCell(row: Int, column: Int): IntCoordinateCellEntity
    fun getCell(coordinate: Coordinate<Int>): IntCoordinateCellEntity
    fun getValue(row: Int, column: Int): ValueType
    fun getValue(coordinate: Coordinate<Int>): ValueType
    fun toValueTable(): List<List<ValueType>>
}