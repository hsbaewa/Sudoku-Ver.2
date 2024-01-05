package kr.co.hs.sudoku.usecase

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

class RandomCreateSudoku(
    private val size: Int,
    private val level: Double,
) {
    fun get(): JsonObject {
        val data = mapOf(
            0 to level,
            1 to 100 - level
        )

        val rowArray = JsonArray()
        repeat(size) {
            val columnArray = JsonArray()
            repeat(size) { columnArray.add(getWeightedRandom(data)) }
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

    fun getIntMatrix(): IntMatrix {
        val data = mapOf(
            0 to level,
            1 to 100 - level
        )

        val rowArray = ArrayList<List<Int>>()
        repeat(size) {
            val columnArray = ArrayList<Int>()
            repeat(size) { columnArray.add(getWeightedRandom(data)) }
            rowArray.add(columnArray)
        }

        return CustomMatrix(rowArray)
    }

    private fun <T> getWeightedRandom(data: Map<T, Double>): T {
        var result = data.keys.first()
        var bestValue = Double.MAX_VALUE

        data.keys.forEach {
            val value = -ln(Random.nextDouble()) / (data[it] ?: 0.0)
            if (value < bestValue) {
                bestValue = value
                result = it
            }
        }

        return result
    }
}