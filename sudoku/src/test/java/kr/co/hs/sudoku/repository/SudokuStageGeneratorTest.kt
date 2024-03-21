package kr.co.hs.sudoku.repository

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration

class SudokuStageGeneratorTest {

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        var matrix = listOf(
            listOf(1, 0, 0, 1),
            listOf(0, 1, 1, 0),
            listOf(1, 0, 0, 1),
            listOf(0, 1, 1, 0)
        )

        var stage = SudokuStageGenerator(matrix).build(this)
        assertNotNull(stage)
        assertFalse(stage.isSudokuClear())
        assertTrue(stage.getCell(0, 0).isImmutable())
        assertTrue(stage.getCell(0, 1).isEmpty())

        matrix = listOf(
            listOf(1, 1, 0, 0, 1, 0, 0, 0, 0),
            listOf(1, 0, 0, 1, 1, 1, 0, 0, 0),
            listOf(1, 1, 0, 0, 0, 0, 0, 1, 0),
            listOf(1, 0, 0, 0, 1, 0, 0, 0, 1),
            listOf(1, 0, 0, 1, 0, 1, 0, 0, 1),
            listOf(1, 0, 0, 0, 1, 0, 0, 0, 1),
            listOf(0, 1, 0, 0, 0, 0, 1, 1, 0),
            listOf(0, 0, 0, 1, 1, 1, 0, 0, 1),
            listOf(0, 0, 0, 0, 1, 0, 0, 1, 1)
        )
        stage = SudokuStageGenerator(matrix).build(this)
        assertNotNull(stage)
        assertFalse(stage.isSudokuClear())
        assertTrue(stage.getCell(0, 0).isImmutable())
        assertTrue(stage.getCell(0, 1).isImmutable())
        assertTrue(stage.getCell(0, 2).isEmpty())
    }
}