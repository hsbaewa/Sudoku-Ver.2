package kr.co.hs.sudoku.model.matrix

import kotlin.math.sqrt

data class CustomMatrix(
    override val boxSize: Int,
    override val boxCount: Int,
    override val rowCount: Int = boxSize * boxCount,
    override val columnCount: Int = rowCount,
    private val matrix: List<List<Int>> = List(rowCount) { List(columnCount) { 0 } }
) : IntMatrix.Custom, List<List<Int>> by matrix {

    constructor(
        matrix: List<List<Int>>
    ) : this(
        boxSize = sqrt(matrix.size.toDouble()).toInt(),
        boxCount = sqrt(matrix.size.toDouble()).toInt(),
        rowCount = matrix.size,
        columnCount = matrix[0].size,
        matrix = matrix
    )

    override fun get(row: Int, column: Int) = matrix[row][column]
    override fun set(row: Int, column: Int, value: Int) = with(matrix[row]) {
        if (this is MutableList) {
            this[column] = value
        } else {
            this.toMutableList()[column] = value
        }
    }
}