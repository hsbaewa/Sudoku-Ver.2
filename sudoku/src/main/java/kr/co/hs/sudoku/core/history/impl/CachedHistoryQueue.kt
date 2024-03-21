package kr.co.hs.sudoku.core.history.impl

import kr.co.hs.sudoku.core.IntCoordinateCellEntity
import kr.co.hs.sudoku.core.Stage
import kr.co.hs.sudoku.core.history.HistoryItem
import kr.co.hs.sudoku.core.history.HistoryQueue
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.TreeSet
import kotlin.math.sqrt

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

        val rowCount = bufferedReader.readLine().toInt()
        val matrix = List(rowCount) { _ ->
            bufferedReader.readLine().split(",").map { it.toInt() }.toMutableList()
        }
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
        val origin = stage.toValueTable()
//            CustomMatrix(stage.toValueTable())

        val boxSize = sqrt(origin.size.toDouble()).toInt()
        val boxCount = sqrt(origin.size.toDouble()).toInt()
        val rowCount = origin.size
        val columnCount = if (origin.isNotEmpty()) origin[0].size else 0

        outputStreamWriter.appendLine(boxSize.toString())
        outputStreamWriter.appendLine(boxCount.toString())
        outputStreamWriter.appendLine(rowCount.toString())
        outputStreamWriter.appendLine(columnCount.toString())
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

    fun createHeader(origin: List<List<Int>>) {
        val boxSize = sqrt(origin.size.toDouble()).toInt()
        val boxCount = sqrt(origin.size.toDouble()).toInt()
        val rowCount = origin.size
        val columnCount = if (origin.isNotEmpty()) origin[0].size else 0

        outputStreamWriter.appendLine(boxSize.toString())
        outputStreamWriter.appendLine(boxCount.toString())
        outputStreamWriter.appendLine(rowCount.toString())
        outputStreamWriter.appendLine(columnCount.toString())
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