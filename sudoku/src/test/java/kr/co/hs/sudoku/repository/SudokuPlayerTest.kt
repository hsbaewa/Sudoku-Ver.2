package kr.co.hs.sudoku.repository

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration

class SudokuPlayerTest {

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        val matrix = listOf(
            listOf(3, 4, 1, 2),
            listOf(0, 1, 4, 0),
            listOf(1, 0, 0, 4),
            listOf(0, 3, 2, 0)
        )

        val stage = SudokuStageBuilder(matrix).build(this)
        SudokuPlayer(stage, 0).flow.collect()

        assertEquals(true, stage.isSudokuClear())
    }
}