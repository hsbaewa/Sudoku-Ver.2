package kr.co.hs.sudoku.model.gamelog.impl

import kr.co.hs.sudoku.model.gamelog.CellLogEntity

data class CellChangedLogEntity(
    override val row: Int,
    override val column: Int,
    override val value: Int,
    override val time: Long
) : CellLogEntity.Changed