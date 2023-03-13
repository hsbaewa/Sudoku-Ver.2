package kr.co.hs.sudoku.model.stage

interface CellCollection<ValueType> {
    val count: Int
    fun toValueList(): List<ValueType>
    fun toList(): List<CellEntity<ValueType>>
    fun filter(value: ValueType): List<CellEntity<ValueType>>
    fun isEmpty(): Boolean
    fun size(): Int
}