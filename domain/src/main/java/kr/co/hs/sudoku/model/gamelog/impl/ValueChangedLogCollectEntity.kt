package kr.co.hs.sudoku.model.gamelog.impl

import kr.co.hs.sudoku.model.gamelog.CellLogCollectEntity
import kr.co.hs.sudoku.model.gamelog.CellLogEntity
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import java.util.*

abstract class ValueChangedLogCollectEntity : CellLogCollectEntity,
    IntCoordinateCellEntity.ValueChangedListener, Queue<CellLogEntity> by LinkedList() {

    override fun onChanged(cell: IntCoordinateCellEntity) {
        if (getPassedTime() < 0)
            return

        add(
            CellChangedLogEntity(
                row = cell.row,
                column = cell.column,
                value = runCatching { cell.getValue() }.getOrDefault(0),
                time = getPassedTime()
            )
        )
    }


    abstract fun getPassedTime(): Long
}