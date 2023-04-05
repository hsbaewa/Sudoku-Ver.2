package kr.co.hs.sudoku.model.matrix

sealed interface IntMatrix : List<List<Int>> {
    val boxSize: Int
    val boxCount: Int
    val rowCount: Int
    val columnCount: Int

    operator fun get(row: Int, column: Int): Int
    operator fun set(row: Int, column: Int, value: Int)

    interface Beginner : IntMatrix
    interface Intermediate : IntMatrix
    interface Advanced : IntMatrix
    interface Custom : IntMatrix
}