package kr.co.hs.sudoku.model.stage.history.impl

import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.model.stage.history.HistoryItem
import kr.co.hs.sudoku.model.stage.history.HistoryQueue
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.TreeSet

class CachedHistoryQueue(outputStream: OutputStream) : HistoryQueue {
    private val treeSet = TreeSet<HistoryItem>()

    private val outputStreamWriter = OutputStreamWriter(outputStream)

    override fun push(
        cell: IntCoordinateCellEntity,
        time: Long,
        completed: Boolean
    ) = cell.run {
        val value = if (isEmpty()) 0 else cell.getValue()
        val result = if (value == 0) {
            treeSet.add(HistoryItem.Removed(row, column, time))
        } else {
            treeSet.add(HistoryItem.Set(row, column, time, getValue(), completed))
        }

        if (result) {
            outputStreamWriter.appendLine(buildString {
                append(row)
                append("-")
                append(column)
                append("-")
                append(value)
                append("-")
                append(if (completed) 1 else 0)
                append("-")
                append(time)
            })
            outputStreamWriter.flush()
        }

        result
    }

    override fun pop(toTimeStamp: Long) =
        treeSet.filter { it.time <= toTimeStamp }
            .also { treeSet.removeAll(it) }
            .takeIf { it.isNotEmpty() }

    override fun isEmpty() = treeSet.isEmpty()

    fun load(inputStream: InputStream): List<List<Int>> {
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)

        val boxSize = bufferedReader.readLine().toInt()
        val boxCount = bufferedReader.readLine().toInt()
        val rowCount = bufferedReader.readLine().toInt()
        val columnCount = bufferedReader.readLine().toInt()
        val matrix = List(rowCount) { _ ->
            bufferedReader.readLine().split(",").map { it.toInt() }.toMutableList()
        }
        val origin = CustomMatrix(boxSize, boxCount, rowCount, columnCount, matrix)
        val lastStatus = matrix.toList()

        do {
            val getLine = bufferedReader.readLine() ?: break
            val line = getLine.split("-")
            val row = line[0].toInt()
            val column = line[1].toInt()
            val value = line[2].toInt()
            val isComplete = line[3] == "1"
            val time = line[4].toLong()
            val item = if (value == 0) {
                lastStatus[row][column] = 0
                HistoryItem.Removed(row, column, time)
            } else {
                lastStatus[row][column] = value
                HistoryItem.Set(row, column, time, value, isComplete)
            }

            treeSet.add(item)
        } while (getLine.isNotEmpty())

        return lastStatus
    }

    fun createHeader(stage: Stage) {
        val origin = CustomMatrix(stage.toValueTable())
        outputStreamWriter.appendLine(origin.boxSize.toString())
        outputStreamWriter.appendLine(origin.boxCount.toString())
        outputStreamWriter.appendLine(origin.rowCount.toString())
        outputStreamWriter.appendLine(origin.columnCount.toString())
        origin.forEach {
            val line = buildString {
                it.forEachIndexed { index, i ->
                    if (index > 0)
                        append(",")
                    append(i)
                }
            }
            outputStreamWriter.appendLine(line)
        }
        outputStreamWriter.flush()
    }
}