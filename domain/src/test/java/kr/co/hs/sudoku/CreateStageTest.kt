package kr.co.hs.sudoku

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.co.hs.sudoku.usecase.RandomCreateSudoku
import org.junit.Test

class CreateStageTest {

    @Test
    fun createBeginner() {

        val dataList = JsonArray()
        val size = 4
        dataList.add(RandomCreateSudoku(size, 40.0).get())
        dataList.add(RandomCreateSudoku(size, 40.0).get())
        dataList.add(RandomCreateSudoku(size, 40.0).get())
        dataList.add(RandomCreateSudoku(size, 40.0).get())
        dataList.add(RandomCreateSudoku(size, 40.0).get())
        dataList.add(RandomCreateSudoku(size, 40.0).get())
        dataList.add(RandomCreateSudoku(size, 50.0).get())
        dataList.add(RandomCreateSudoku(size, 50.0).get())
        dataList.add(RandomCreateSudoku(size, 50.0).get())
        dataList.add(RandomCreateSudoku(size, 50.0).get())
        dataList.add(RandomCreateSudoku(size, 50.0).get())
        dataList.add(RandomCreateSudoku(size, 50.0).get())
        dataList.add(RandomCreateSudoku(size, 60.0).get())
        dataList.add(RandomCreateSudoku(size, 60.0).get())
        dataList.add(RandomCreateSudoku(size, 60.0).get())
        dataList.add(RandomCreateSudoku(size, 60.0).get())
        dataList.add(RandomCreateSudoku(size, 60.0).get())
        dataList.add(RandomCreateSudoku(size, 60.0).get())

        val result = JsonObject()
        result.add("data", dataList)

        val resultJson = Gson().toJson(result)
        println(resultJson)
    }

    @Test
    fun createIntermediate() {

        val dataList = JsonArray()
        val size = 9
        dataList.add(RandomCreateSudoku(size, 40.0).get())
        dataList.add(RandomCreateSudoku(size, 40.0).get())
        dataList.add(RandomCreateSudoku(size, 40.0).get())
        dataList.add(RandomCreateSudoku(size, 40.0).get())
        dataList.add(RandomCreateSudoku(size, 40.0).get())
        dataList.add(RandomCreateSudoku(size, 40.0).get())
        dataList.add(RandomCreateSudoku(size, 50.0).get())
        dataList.add(RandomCreateSudoku(size, 50.0).get())
        dataList.add(RandomCreateSudoku(size, 50.0).get())
        dataList.add(RandomCreateSudoku(size, 50.0).get())
        dataList.add(RandomCreateSudoku(size, 50.0).get())
        dataList.add(RandomCreateSudoku(size, 50.0).get())
        dataList.add(RandomCreateSudoku(size, 60.0).get())
        dataList.add(RandomCreateSudoku(size, 60.0).get())
        dataList.add(RandomCreateSudoku(size, 60.0).get())
        dataList.add(RandomCreateSudoku(size, 60.0).get())
        dataList.add(RandomCreateSudoku(size, 60.0).get())
        dataList.add(RandomCreateSudoku(size, 60.0).get())

        val result = JsonObject()
        result.add("data", dataList)

        val resultJson = Gson().toJson(result)

        println(resultJson)
    }


    @Test
    fun createAdvanced() {

        val dataList = JsonArray()
        val size = 16
        dataList.add(RandomCreateSudoku(size, 40.0).get())
        dataList.add(RandomCreateSudoku(size, 40.0).get())
        dataList.add(RandomCreateSudoku(size, 40.0).get())
        dataList.add(RandomCreateSudoku(size, 40.0).get())
        dataList.add(RandomCreateSudoku(size, 40.0).get())
        dataList.add(RandomCreateSudoku(size, 40.0).get())
        dataList.add(RandomCreateSudoku(size, 50.0).get())
        dataList.add(RandomCreateSudoku(size, 50.0).get())
        dataList.add(RandomCreateSudoku(size, 50.0).get())
        dataList.add(RandomCreateSudoku(size, 50.0).get())
        dataList.add(RandomCreateSudoku(size, 50.0).get())
        dataList.add(RandomCreateSudoku(size, 50.0).get())
        dataList.add(RandomCreateSudoku(size, 60.0).get())
        dataList.add(RandomCreateSudoku(size, 60.0).get())
        dataList.add(RandomCreateSudoku(size, 60.0).get())
        dataList.add(RandomCreateSudoku(size, 60.0).get())
        dataList.add(RandomCreateSudoku(size, 60.0).get())
        dataList.add(RandomCreateSudoku(size, 60.0).get())

        val result = JsonObject()
        result.add("data", dataList)

        val resultJson = Gson().toJson(result)

        println(resultJson)
    }


}