package kr.co.hs.sudoku.core.impl

import kr.co.hs.sudoku.core.CellEntity
import kr.co.hs.sudoku.core.Coordinate
import kr.co.hs.sudoku.core.IntCoordinateCellEntity

data class IntCoordinateCellEntityImpl(
    override val coordinate: Coordinate<Int>,
    private val cell: CellEntity<Int> = IntCellEntityImpl()
) : IntCoordinateCellEntity, CellEntity<Int> by cell {
    override val row = coordinate.x
    override val column = coordinate.y
    override var valueChangedListener: IntCoordinateCellEntity.ValueChangedListener? = null

    constructor(
        row: Int,
        column: Int
    ) : this(IntCoordinate(row, column))

    override fun toEmpty() {
        this.cell.toEmpty()
        this.valueChangedListener?.onChanged(this)
    }

    override fun toImmutable() {
        this.cell.toImmutable()
        this.valueChangedListener?.onChanged(this)
    }

    override fun toImmutable(value: Int) {
        this.cell.toImmutable(value)
        this.valueChangedListener?.onChanged(this)
    }

    override fun toMutable() {
        this.cell.toMutable()
        this.valueChangedListener?.onChanged(this)
    }

    override fun toMutable(value: Int) {
        this.cell.toMutable(value)
        this.valueChangedListener?.onChanged(this)
    }

    override fun setValue(value: Int) {
        this.cell.setValue(value)
        this.valueChangedListener?.onChanged(this)
    }
}