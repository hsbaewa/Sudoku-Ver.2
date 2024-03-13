package kr.co.hs.sudoku.core

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.usecase.SudokuRandomGenerateUseCase
import org.junit.Before
import org.junit.Test
import kotlin.math.sqrt
import kotlin.time.Duration

class CreateStageTest {

    lateinit var sudokuRandomGenerator: SudokuRandomGenerateUseCase

    @Before
    fun init() {
        sudokuRandomGenerator = SudokuRandomGenerateUseCase()
    }

    fun Stage.toJson(): JsonObject {
        val rowArray = JsonArray()
        repeat(rowCount) { row ->
            val columnArray = JsonArray()
            repeat(columnCount) { column ->
                columnArray.add(
                    this.runCatching { get(row, column) }.getOrDefault(0)
                )
            }
            rowArray.add(columnArray)
        }

        val resultObj = JsonObject()
        resultObj.addProperty("boxSize", sqrt(rowArray.size().toDouble()).toInt())
        resultObj.addProperty("boxCount", sqrt(rowArray.size().toDouble()).toInt())
        resultObj.addProperty("width", rowArray.size())
        resultObj.addProperty("height", rowArray.size())
        resultObj.add("matrix", rowArray)

        return resultObj
    }

    @Test
    fun createBeginner() = runTest(timeout = Duration.INFINITE) {

        val dataList = JsonArray()
        val size = 4
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson()
        )



        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson()
        )



        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson()
        )

        val result = JsonObject()
        result.add("data", dataList)

        val resultJson = Gson().toJson(result)
        println(resultJson)
    }

    @Test
    fun createIntermediate() = runTest(timeout = Duration.INFINITE) {

        val dataList = JsonArray()
        val size = 9
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson()
        )
        val result = JsonObject()
        result.add("data", dataList)

        val resultJson = Gson().toJson(result)

        println(resultJson)
    }


    @Test
    fun createAdvanced() = runTest(timeout = Duration.INFINITE) {

        val dataList = JsonArray()
        val size = 16
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson()
        )
        dataList.add(
            sudokuRandomGenerator(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson()
        )

        val result = JsonObject()
        result.add("data", dataList)

        val resultJson = Gson().toJson(result)

        println(resultJson)
    }


}