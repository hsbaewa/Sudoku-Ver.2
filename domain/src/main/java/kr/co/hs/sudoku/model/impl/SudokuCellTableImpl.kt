package kr.co.hs.sudoku.model.impl

import kr.co.hs.sudoku.model.CellTable
import kr.co.hs.sudoku.model.Coordinate
import kr.co.hs.sudoku.model.IntCoordinateCellEntity

open class SudokuCellTableImpl(
    override val rowCount: Int,
    override val columnCount: Int,
    private val table: List<List<IntCoordinateCellEntity>>
) : CellCollectionImpl<Int>(rowCount * columnCount),
    CellTable<Int> {

    constructor(
        rowCount: Int,
        columnCount: Int
    ) : this(rowCount, columnCount,
        List(rowCount) { x ->
            List(columnCount) { y ->
                IntCoordinateCellEntityImpl(IntCoordinate(x, y))
            }
        }
    )

    /**
     * CellCollection
     */
    override fun toList() = table.flatten()


    /**
     * CellTable
     */
    override fun getCell(row: Int, column: Int) = this.table[row][column]
    override fun getCell(coordinate: Coordinate<Int>) = getCell(coordinate.x, coordinate.y)
    override fun getValue(row: Int, column: Int) = getCell(row, column).getValue()
    override fun getValue(coordinate: Coordinate<Int>) = getCell(coordinate).getValue()
    override fun toValueTable() =
        this.table.map { row ->
            row.map { column ->
                if (column.isEmpty())
                    0
                else
                    column.getValue()
            }
        }
}