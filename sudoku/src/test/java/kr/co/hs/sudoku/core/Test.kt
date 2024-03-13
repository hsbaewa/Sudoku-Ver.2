package kr.co.hs.sudoku.core

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.RandomSudokuStageGenerator
import kr.co.hs.sudoku.SudokuStageBuilder
import kr.co.hs.sudoku.SudokuStageGenerator
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.time.Duration

class Test {

    @Test
    fun sudoku_build_test() = runTest(timeout = Duration.INFINITE) {
        val matrix = listOf(
            listOf(3, 4, 1, 2),
            listOf(0, 1, 4, 0),
            listOf(1, 0, 0, 4),
            listOf(0, 3, 2, 0)
        )

        val builder = SudokuStageBuilder(matrix)
        val stage = builder.build(this)
        assertNotNull(stage)

        assertEquals(stage[0, 0], 3)
        assertTrue(stage.getCell(0, 0).isImmutable())

//        assertEquals(stage[1, 0], 0)
        assertTrue(stage.getCell(1, 0).isEmpty())

        assertThrows(Exception::class.java) {
            stage[0, 0] = 2
        }

        stage[1, 0] = 2
        stage[1, 3] = 3
        stage[2, 1] = 2
        stage[2, 2] = 3
        stage[3, 0] = 4
        stage[3, 3] = 1

        assertTrue(stage.isSudokuClear())
    }

    @Test
    fun sudoku_generate_test() = runTest(timeout = Duration.INFINITE) {
        assertNotNull(
            SudokuStageGenerator(
                listOf(
                    listOf(1, 0, 0, 1),
                    listOf(0, 1, 1, 0),
                    listOf(1, 0, 0, 1),
                    listOf(0, 1, 1, 0)
                )
            ).build(this)
        )


        assertNotNull(
            SudokuStageGenerator(
                listOf(
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
            ).build(this)
        )
    }

    @Test
    fun sudoku_random_generate_test() = runTest(timeout = Duration.INFINITE) {
        assertNotNull(RandomSudokuStageGenerator(4, 10.0).build(this))
        assertNotNull(RandomSudokuStageGenerator(4, 20.0).build(this))
        assertNotNull(RandomSudokuStageGenerator(4, 50.0).build(this))
        assertNotNull(RandomSudokuStageGenerator(4, 70.0).build(this))

        assertNotNull(RandomSudokuStageGenerator(9, 10.0).build(this))
        assertNotNull(RandomSudokuStageGenerator(9, 20.0).build(this))
        assertNotNull(RandomSudokuStageGenerator(9, 50.0).build(this))
        assertNotNull(RandomSudokuStageGenerator(9, 70.0).build(this))
    }
}