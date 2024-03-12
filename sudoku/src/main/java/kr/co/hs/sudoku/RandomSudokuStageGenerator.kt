package kr.co.hs.sudoku

import kotlin.math.ln
import kotlin.random.Random

class RandomSudokuStageGenerator(
    private val size: Int,
    private val level: Double
) : SudokuBuilder {
    override fun build() = SudokuStageGenerator(
        ArrayList<List<Int>>().apply {
            val data = mapOf(
                0 to level,
                1 to 100 - level
            )
            repeat(this@RandomSudokuStageGenerator.size) {
                add(ArrayList<Int>().apply {
                    repeat(this@RandomSudokuStageGenerator.size) {
                        add(getWeightedRandom(data))
                    }
                })
            }
        }
    ).build()

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