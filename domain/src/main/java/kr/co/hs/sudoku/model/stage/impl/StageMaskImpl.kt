package kr.co.hs.sudoku.model.stage.impl

import kr.co.hs.sudoku.model.stage.MutableStage
import kr.co.hs.sudoku.model.stage.StageMask

class StageMaskImpl(
    private val mask: List<List<Int>>
) : StageMask {
    override fun setMask(mutableStage: MutableStage) {
        for (row in 0 until mutableStage.rowCount) {
            for (column in 0 until mutableStage.columnCount) {
                val cell = mutableStage.getCell(row, column)
                if (mask[row][column] == 0) {
                    cell.toMutable()
                } else {
                    cell.toImmutable()
                }
            }
        }
        mutableStage.clear()
    }

}