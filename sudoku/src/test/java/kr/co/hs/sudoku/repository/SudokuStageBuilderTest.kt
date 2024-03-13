package kr.co.hs.sudoku.repository

import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import kotlin.time.Duration

class SudokuStageBuilderTest {
    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        val matrix = listOf(
            listOf(3, 4, 1, 2),
            listOf(0, 1, 4, 0),
            listOf(1, 0, 0, 4),
            listOf(0, 3, 2, 0)
        )

        val stage = SudokuStageBuilder(matrix).build(this)
        TestCase.assertNotNull(stage)

        TestCase.assertEquals(stage[0, 0], 3)
        TestCase.assertTrue(stage.getCell(0, 0).isImmutable())

//        assertEquals(stage[1, 0], 0)
        TestCase.assertTrue(stage.getCell(1, 0).isEmpty())

        Assert.assertThrows(Exception::class.java) {
            stage[0, 0] = 2
        }

        stage[1, 0] = 2
        stage[1, 3] = 3
        stage[2, 1] = 2
        stage[2, 2] = 3
        stage[3, 0] = 4
        stage[3, 3] = 1

        TestCase.assertTrue(stage.isSudokuClear())
    }
}