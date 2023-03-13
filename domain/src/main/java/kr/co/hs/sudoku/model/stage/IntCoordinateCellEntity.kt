package kr.co.hs.sudoku.model.stage

interface IntCoordinateCellEntity : CellEntity<Int> {
    val coordinate: Coordinate<Int>
    val row: Int
    val column: Int
    var valueChangedListener: ValueChangedListener?

    interface ValueChangedListener {
        fun onChanged(cell: IntCoordinateCellEntity)
    }
}