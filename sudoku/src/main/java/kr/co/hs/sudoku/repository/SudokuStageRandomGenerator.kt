package kr.co.hs.sudoku.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.core.Stage
import kotlin.math.ln
import kotlin.random.Random

internal class SudokuStageRandomGenerator(
    private val size: Int,
    private val level: Double
) : SudokuBuilder {
    private fun generateRandomFixCell() = ArrayList<List<Int>>().apply {
        val data = mapOf(
            0 to level,
            1 to 100 - level
        )
        repeat(this@SudokuStageRandomGenerator.size) {
            add(ArrayList<Int>().apply {
                repeat(this@SudokuStageRandomGenerator.size) {
                    add(getWeightedRandom(data))
                }
            })
        }
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

    override val flow: Flow<Stage> = SudokuStageGenerator(generateRandomFixCell()).flow

    override suspend fun build(scope: CoroutineScope): Stage = scope
        .async { withContext(Dispatchers.Default) { flow.first() } }
        .await()

    override fun build(): Stage = runBlocking { withContext(Dispatchers.Default) { flow.first() } }

}