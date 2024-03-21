package kr.co.hs.sudoku.usecase

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.core.Stage
import org.junit.Before
import org.junit.Test
import kotlin.math.sqrt
import kotlin.time.Duration

class SudokuRandomGenerateUseCaseTest {
    private lateinit var sudokuRandomGenerateUseCase: SudokuRandomGenerateUseCase

    @Before
    fun before() {
        sudokuRandomGenerateUseCase = SudokuRandomGenerateUseCase()
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        var stage = sudokuRandomGenerateUseCase(SudokuRandomGenerateUseCase.Param(4, 50.0), this)

        assertNotNull(stage)
        assertEquals(4, stage.rowCount)
        assertFalse(stage.isSudokuClear())


        stage = sudokuRandomGenerateUseCase(SudokuRandomGenerateUseCase.Param(9, 50.0), this)

        assertEquals(9, stage.rowCount)
        assertFalse(stage.isSudokuClear())
    }

    private fun Stage.toJson(): JsonObject {
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
    fun create_beginner_test() = runTest(timeout = Duration.INFINITE) {

        val dataList = JsonArray()
        val size = 4
        val usecase = sudokuRandomGenerateUseCase
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson())

        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson())

        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson())

        val result = JsonObject()
        result.add("data", dataList)

        val resultJson = Gson().toJson(result)
        println(resultJson)
    }

    @Test
    fun create_intermediate_test() = runTest(timeout = Duration.INFINITE) {

        val dataList = JsonArray()
        val size = 9
        val usecase = sudokuRandomGenerateUseCase
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson())

        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson())

        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson())
        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson())

        val result = JsonObject()
        result.add("data", dataList)

        val resultJson = Gson().toJson(result)
        println(resultJson)
    }

//    @Test
//    fun create_advanced_test() = runTest(timeout = Duration.INFINITE) {
//
//        val dataList = JsonArray()
//        val size = 16
//        val usecase = sudokuRandomGenerateUseCase
//        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson())
//        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson())
//        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson())
//        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson())
//        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson())
//        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 40.0), this).toJson())
//
//        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson())
//        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson())
//        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson())
//        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson())
//        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson())
//        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 50.0), this).toJson())
//
//        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson())
//        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson())
//        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson())
//        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson())
//        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson())
//        dataList.add(usecase(SudokuRandomGenerateUseCase.Param(size, 60.0), this).toJson())
//
//        val result = JsonObject()
//        result.add("data", dataList)
//
//        val resultJson = Gson().toJson(result)
//        println(resultJson)
//    }
}