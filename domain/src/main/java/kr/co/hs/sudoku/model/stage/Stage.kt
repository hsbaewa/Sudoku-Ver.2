package kr.co.hs.sudoku.model.stage

interface Stage : CellTable<Int>, SudokuStrategyRule {
    fun getBoxBoundedIn(cell: IntCoordinateCellEntity): SudokuBox
    fun getBox(row: Int, column: Int): SudokuBox
    fun getRowLine(row: Int): SudokuLine
    fun getColumnLine(column: Int): SudokuLine
    fun getAroundBox(coordinate: Coordinate<Int>): List<SudokuBox>

    fun minValue(): Int
    fun maxValue(): Int

    fun clear(row: Int, column: Int) = getCell(row, column).toEmpty()

    operator fun get(row: Int, column: Int) = getValue(row, column)
    operator fun set(row: Int, column: Int, value: Int) {
        if (value < minValue() || value > maxValue()) {
            clear(row, column)
        } else {
            getCell(row, column).setValue(value)
        }
    }

    fun setValueChangedListener(valueChangedListener: IntCoordinateCellEntity.ValueChangedListener)

    fun getAvailable(row: Int, column: Int): List<Int>
}