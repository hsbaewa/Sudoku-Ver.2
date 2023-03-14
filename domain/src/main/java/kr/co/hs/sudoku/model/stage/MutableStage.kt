package kr.co.hs.sudoku.model.stage

interface MutableStage : Stage, MutableCellTable<Int> {
    override fun getBoxBoundedIn(cell: IntCoordinateCellEntity): MutableSudokuBox
    override fun getBox(row: Int, column: Int): MutableSudokuBox
    override fun getRowLine(row: Int): MutableSudokuLine
    override fun getColumnLine(column: Int): MutableSudokuLine
    override fun getAroundBox(coordinate: Coordinate<Int>): List<MutableSudokuBox>
}