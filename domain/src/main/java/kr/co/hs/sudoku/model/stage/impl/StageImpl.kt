package kr.co.hs.sudoku.model.stage.impl

import kr.co.hs.sudoku.model.stage.*
import kr.co.hs.sudoku.model.stage.history.HistoryQueue
import kr.co.hs.sudoku.repository.timer.Timer
import kotlin.math.pow

@Suppress("unused")
open class StageImpl(
    private val boxSize: Int,
    private val boxCount: Int,
    private val table: List<List<IntCoordinateCellEntity>>,
    private val boxTable: List<List<SudokuBox>>,
    private val rowLine: List<SudokuLine>,
    private val columnLine: List<SudokuLine>,
) : Stage,
    SudokuCellTableImpl(boxSize * boxCount, boxSize * boxCount, table),
    IntCoordinateCellEntity.ValueChangedListener {

    private val listenerSet = HashSet<IntCoordinateCellEntity.ValueChangedListener>()

    constructor(
        boxSize: Int,
        boxCount: Int,
        table: List<List<IntCoordinateCellEntity>>
    ) : this(
        boxSize,
        boxCount,
        table,
        boxTable = List(boxCount) { row ->
            List(boxCount) { column ->


                MutableSudokuBoxImpl(boxSize, boxSize).apply {
                    for (x in 0 until boxSize) {
                        for (y in 0 until boxSize) {
                            setCell(
                                x, y,
                                table[(row * boxSize) + x][(column * boxSize) + y]
                            )
                        }

                    }
                }


            }
        },
        rowLine = List(boxSize * boxCount) { row ->
            MutableSudokuLineImpl(boxSize * boxCount).apply {
                for (idx in 0 until (boxSize * boxCount)) {
                    setCell(idx, table[row][idx])
                }
            }
        },
        columnLine = List(boxSize * boxCount) { column ->
            MutableSudokuLineImpl(boxSize * boxCount).apply {
                for (idx in 0 until (boxSize * boxCount)) {
                    setCell(idx, table[idx][column])
                }
            }

        }
    )

    constructor(
        boxSize: Int,
        boxCount: Int
    ) : this(
        boxSize,
        boxCount,
        List(boxSize * boxCount) { row ->
            List(boxSize * boxCount) { column ->
                IntCoordinateCellEntityImpl(row, column)
            }
        }
    )

    override fun getBoxBoundedIn(cell: IntCoordinateCellEntity) =
        this.boxTable[cell.coordinate.x / boxCount][cell.coordinate.y / boxCount]

    override fun getBox(row: Int, column: Int) = this.boxTable[row][column]
    override fun getRowLine(row: Int) = rowLine[row]
    override fun getColumnLine(column: Int) = columnLine[column]
    override fun getAroundBox(coordinate: Coordinate<Int>): List<SudokuBox> {
        return buildList {
            val boxRow = coordinate.x / boxCount
            val boxColumn = coordinate.y / boxCount
            if (boxRow - 1 >= 0) {
                add(getBox(boxRow - 1, boxColumn))
            }
            if (boxRow + 1 < boxCount) {
                add(getBox(boxRow + 1, boxColumn))
            }
            if (boxColumn - 1 >= 0) {
                add(getBox(boxRow, boxColumn - 1))
            }
            if (boxColumn + 1 < boxColumn) {
                add(getBox(boxRow, boxColumn + 1))
            }
        }
    }

    override fun minValue() = 1
    override fun maxValue() = boxSize.toDouble().pow(2).toInt()
    override fun addValueChangedListener(valueChangedListener: IntCoordinateCellEntity.ValueChangedListener) {
        this.table.flatten().forEach { it.valueChangedListener = this }
        this.listenerSet.add(valueChangedListener)
    }

    override fun removeValueChangedListener(valueChangedListener: IntCoordinateCellEntity.ValueChangedListener) {
        this.listenerSet.remove(valueChangedListener)
    }

    override fun getAvailable(row: Int, column: Int): List<Int> {
        val cell = getCell(row, column)
        val box = getBoxBoundedIn(cell).getAvailableValueInBox().toSet()
        val rowLine = getRowLine(row).getAvailableValueInLine().toSet()
        val columnLine = getColumnLine(column).getAvailableValueInLine().toSet()

        val result = box.intersect(rowLine).intersect(columnLine)
        return result.toList()
    }

    /**
     * SudokuStrategyRule
     */
    override fun setCellCollection(collection: CellCollection<Int>) {}
    override fun getDuplicatedCells(): CellList<Int> {
        val result = buildList {
            addAll(boxTable.flatten().flatMap { it.getDuplicatedCells().toList() })
            addAll(rowLine.flatMap { it.getDuplicatedCells().toList() })
            addAll(columnLine.flatMap { it.getDuplicatedCells().toList() })
        }.distinct()
        return SudokuCellListImpl(result.size, result)
    }

    override fun getDuplicatedCellCount() = getDuplicatedCells().count

    override fun getEmptyCells(): CellList<Int> {
        val result = table.flatten()
            .filter { it.isEmpty() }
        return SudokuCellListImpl(result.size, result)
    }

    override fun getEmptyCellCount() = getEmptyCells().count

    override fun isSudokuClear() =
        getDuplicatedCells().isEmpty() && getEmptyCells().isEmpty()

    override fun setTimer(timer: Timer) {
        this.timer = timer
    }

    private lateinit var timer: Timer

    /**
     * ValueChangeListener
     */
    override fun onChanged(cell: IntCoordinateCellEntity) {
        if (this::timer.isInitialized) {
            val time = timer.getPassedTime()
            val isComplete = isSudokuClear()
            if (isComplete)
                completeTime = time

            historyQueue?.push(cell, time, isComplete)
        }
        val list = listenerSet.toList()
        list.forEach { it.onChanged(cell) }
    }

    override fun getClearTime() = completeTime
    private var completeTime = -1L

    override fun startCaptureHistory(writer: HistoryQueue) {
        this.historyQueue = writer
    }

    private var historyQueue: HistoryQueue? = null

    override fun stopCaptureHistory() {
        historyQueue = null
    }
}