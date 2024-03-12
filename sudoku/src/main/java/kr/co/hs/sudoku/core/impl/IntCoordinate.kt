package kr.co.hs.sudoku.core.impl

import kr.co.hs.sudoku.core.Coordinate

data class IntCoordinate(
    override val x: Int,
    override val y: Int
) : Coordinate<Int>