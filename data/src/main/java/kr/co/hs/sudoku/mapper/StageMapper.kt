package kr.co.hs.sudoku.mapper

import kr.co.hs.sudoku.model.matrix.IntMatrix

object StageMapper {
    inline fun <reified T : IntMatrix> List<List<Int>>.toDomain(): T {
        val instance = T::class.java.getConstructor().newInstance()
        (0 until instance.rowCount).forEach { row ->
            (0 until instance.columnCount).forEach { column ->
                instance[row, column] = this[row][column]
            }
        }
        return instance
    }
}