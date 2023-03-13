package kr.co.hs.sudoku.model

interface CellEntity<ValueType> {
    val value: CellValueEntity<ValueType>

    fun isMutable(): Boolean
    fun toMutable()
    fun toMutable(value: ValueType)
    fun isImmutable(): Boolean
    fun toImmutable()
    fun toImmutable(value: ValueType)
    fun isEmpty(): Boolean
    fun toEmpty()

    fun getValue(): ValueType
    fun setValue(value: ValueType)
}