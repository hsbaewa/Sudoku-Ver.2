package kr.co.hs.sudoku.feature.matrixlist

import kr.co.hs.sudoku.model.matrix.IntMatrix

sealed class MatrixListItem {
    data class MatrixItem(val matrix: IntMatrix) : MatrixListItem()
    data class HeaderItem(val header: String) : MatrixListItem()
    data class TitleItem(val title: String) : MatrixListItem()
}