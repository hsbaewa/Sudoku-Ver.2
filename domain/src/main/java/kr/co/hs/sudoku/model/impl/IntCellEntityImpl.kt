package kr.co.hs.sudoku.model.impl

import kr.co.hs.sudoku.model.CellEntity
import kr.co.hs.sudoku.model.CellValueEntity

class IntCellEntityImpl : CellEntity<Int> {

    override var value: CellValueEntity<Int> = CellValueEntity.Empty

    override fun isMutable() = value is CellValueEntity.Mutable
    override fun toMutable() {
        this.value = CellValueEntity.Mutable(this.value.getValue())
    }

    override fun toMutable(value: Int) {
        this.value = CellValueEntity.Mutable(value)
    }

    override fun isImmutable() = value is CellValueEntity.Immutable
    override fun toImmutable() {
        this.value = CellValueEntity.Immutable(this.value.getValue())
    }

    override fun toImmutable(value: Int) {
        this.value = CellValueEntity.Immutable(value)
    }

    override fun isEmpty() = value is CellValueEntity.Empty
    override fun toEmpty() {
        value = CellValueEntity.Empty
    }

    override fun getValue(): Int {
        return value()
    }

    override fun setValue(value: Int) {
        when (val v = this.value) {
            is CellValueEntity.Immutable -> throw IllegalArgumentException("is immutable value")
            CellValueEntity.Empty -> toMutable(value)
            is CellValueEntity.Mutable -> v.setValue(value)
        }
    }
}