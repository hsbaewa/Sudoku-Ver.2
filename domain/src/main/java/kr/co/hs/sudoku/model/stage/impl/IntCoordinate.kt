package kr.co.hs.sudoku.model.stage.impl

import kr.co.hs.sudoku.model.stage.Coordinate

data class IntCoordinate(
    override val x: Int,
    override val y: Int
) : Coordinate<Int>