package kr.co.hs.sudoku.model.stage.impl

import kr.co.hs.sudoku.model.stage.Coordinate
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.MutableCellTable

open class MutableSudokuCellTableImpl(
    override val rowCount: Int,
    override val columnCount: Int,
    private val table: List<MutableList<IntCoordinateCellEntity>>
) : MutableCellTable<Int>,
    SudokuCellTableImpl(rowCount, columnCount, table) {

    constructor(
        rowCount: Int,
        columnCount: Int
    ) : this(rowCount, columnCount,
        List(rowCount) { row ->
            MutableList(columnCount) { column ->
                IntCoordinateCellEntityImpl(IntCoordinate(row, column))
            }
        }
    )

    override fun setCell(row: Int, column: Int, cell: IntCoordinateCellEntity) {
        table[row][column] = cell
    }

    override fun setCell(coordinate: Coordinate<Int>, cell: IntCoordinateCellEntity) =
        setCell(coordinate.x, coordinate.y, cell)

    override fun setValue(row: Int, column: Int, value: Int) {
        table[row][column].run {
            if (isMutable()) {
                setValue(value)
            } else {
                toMutable(value)
            }
        }
    }

    override fun setValue(coordinate: Coordinate<Int>, value: Int) =
        setValue(coordinate.x, coordinate.y, value)

    override fun clear() =
        toList().filter { it.isMutable() }
            .forEach { it.toEmpty() }

    override fun clear(value: Int) =
        toList().filter { it.isMutable() && it.getValue() == value }
            .forEach { it.toEmpty() }
}