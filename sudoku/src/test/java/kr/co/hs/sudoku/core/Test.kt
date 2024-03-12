package kr.co.hs.sudoku.core

import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.RandomSudokuStageGenerator
import kr.co.hs.sudoku.SudokuStageBuilder
import kr.co.hs.sudoku.SudokuStageGenerator
import org.junit.Test

class Test {

    @Test
    fun sudoku_build_test() = runTest {
        val sudokuBuilder = SudokuStageBuilder(
            listOf(
                listOf(3, 4, 1, 2),
                listOf(0, 1, 4, 0),
                listOf(1, 0, 0, 4),
                listOf(0, 3, 2, 0)
            )
        )
        val stage = sudokuBuilder.build().firstOrNull()
        assertNotNull(stage)
    }

    @Test
    fun sudoku_generate_test() = runTest {
        assertNotNull(
            SudokuStageGenerator(
                listOf(
                    listOf(1, 0, 0, 1),
                    listOf(0, 1, 1, 0),
                    listOf(1, 0, 0, 1),
                    listOf(0, 1, 1, 0)
                )
            ).build().firstOrNull()
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
            ).build().firstOrNull()
        )
    }

    @Test
    fun sudoku_random_generate_test() = runTest {
        assertNotNull(RandomSudokuStageGenerator(4, 10.0).build().firstOrNull())
        assertNotNull(RandomSudokuStageGenerator(4, 20.0).build().firstOrNull())
        assertNotNull(RandomSudokuStageGenerator(4, 50.0).build().firstOrNull())
        assertNotNull(RandomSudokuStageGenerator(4, 70.0).build().firstOrNull())

        assertNotNull(RandomSudokuStageGenerator(9, 10.0).build().firstOrNull())
        assertNotNull(RandomSudokuStageGenerator(9, 20.0).build().firstOrNull())
        assertNotNull(RandomSudokuStageGenerator(9, 50.0).build().firstOrNull())
        assertNotNull(RandomSudokuStageGenerator(9, 70.0).build().firstOrNull())
    }
}