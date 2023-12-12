package kr.co.hs.sudoku.model.matrix

data class EmptyMatrix(
    private val matrix: List<List<Int>> = List(0) { List(0) { 0 } }
) : IntMatrix.Custom, List<List<Int>> by matrix {

    override val boxSize = 0
    override val boxCount = 0
    override val rowCount = 0
    override val columnCount = 0


    override fun get(row: Int, column: Int) = matrix[row][column]
    override fun set(row: Int, column: Int, value: Int) = with(matrix[row]) {
        if (this is MutableList) {
            this[column] = value
        } else {
            this.toMutableList()[column] = value
        }
    }
}