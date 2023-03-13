package kr.co.hs.sudoku.model.impl

import kr.co.hs.sudoku.model.Coordinate

data class IntCoordinate(
    override val x: Int,
    override val y: Int
) : Coordinate<Int>