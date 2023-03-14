package kr.co.hs.sudoku.model.stage.impl

import kr.co.hs.sudoku.model.stage.*

class StageBuilderImpl : StageBuilder {
    private var boxSize = 0
    private var boxCount = 0
    private var autoGen = false
    private var autoGenFilter: List<List<Int>> = listOf()
    private var stageTable: List<List<Int>> = listOf()

    override fun setBox(size: Int, count: Int): StageBuilder {
        boxSize = size
        boxCount = count
        return this
    }

    override fun autoGenerate(generateFilter: List<List<Int>>?): StageBuilder {
        autoGen = true
        autoGenFilter = generateFilter ?: listOf()
        return this
    }

    override fun setStage(stageTable: List<List<Int>>): StageBuilder {
        this.stageTable = stageTable
        return this
    }

    override fun build(): Stage {
        val table = List(boxSize * boxCount) { row ->
            val stageRow = if (row < this.stageTable.size) {
                this.stageTable[row]
            } else null
            MutableList<IntCoordinateCellEntity>(boxSize * boxCount) { column ->
                IntCoordinateCellEntityImpl(row, column).apply {
                    if (stageRow != null && column < stageRow.size) {
                        val value = stageRow[column]
                        if (value > 0) {
                            toImmutable(stageRow[column])
                        }
                    }

                }
            }
        }
        val stage = MutableStageImpl(boxSize, boxCount, table)
        if (autoGen) {
            stage.generate(0, 0)

            for (row in 0 until stage.rowCount) {
                val rowList = if (row < autoGenFilter.size) autoGenFilter[row] else null
                for (column in 0 until stage.columnCount) {
                    val cell = stage.getCell(row, column)
                    if (rowList != null && rowList[column] > 0) {
                        cell.toImmutable()
                    }
                }
            }

            stage.clear()
        }
        return stage
    }

    private fun MutableStage.generate(row: Int, column: Int) {
        if (row == rowCount)
            return

        val cell = getCell(row, column)

        val available = getAvailable(row, column).shuffled()
        if (available.isEmpty()) {
            val flattenList = toList()
            val idx = flattenList.indexOf(cell)
            flattenList.subList(idx, size()).forEach {
                if (it.isMutable())
                    it.toEmpty()
            }
            return
        }
        available.forEach {

            if (isCompleted())
                return

            if (!cell.isImmutable())
                this[row, column] = it

            if (column == columnCount - 1) {
                generate(row + 1, 0)
            } else {
                generate(row, column + 1)
            }
        }
    }

}