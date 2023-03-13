package kr.co.hs.sudoku.model.impl

import kr.co.hs.sudoku.model.CellCollection

abstract class CellCollectionImpl<ValueType>(
    override val count: Int
) : CellCollection<ValueType> {
    override fun toValueList() = toList().mapNotNull { if (it.isEmpty()) null else it.getValue() }
    override fun filter(value: ValueType) = toList().filter { it.getValue() == value }
    override fun isEmpty() = toList().isEmpty()
    override fun size() = toList().size
}