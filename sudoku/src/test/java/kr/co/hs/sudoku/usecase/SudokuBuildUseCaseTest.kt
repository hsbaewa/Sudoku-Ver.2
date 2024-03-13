package kr.co.hs.sudoku.usecase

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.usecase.abs.UseCase
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration

class SudokuBuildUseCaseTest {
    private lateinit var sudokuBuildUseCase: SudokuBuildUseCase

    @Before
    fun before() {
        sudokuBuildUseCase = SudokuBuildUseCase()
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        var matrix = listOf(
            listOf(3, 4, 1, 2),
            listOf(0, 1, 4, 0),
            listOf(1, 0, 0, 4),
            listOf(0, 3, 2, 0)
        )

        var stage = sudokuBuildUseCase(matrix, this)

        assertNotNull(stage)
        assertEquals(4, stage.rowCount)
        assertTrue(stage.getDuplicatedCells().isEmpty())
        assertTrue(stage.getCell(0, 0).isImmutable())
        assertTrue(stage.getCell(1, 0).isEmpty())


        matrix = listOf(
            listOf(5, 3, 0, 0, 7, 0, 0, 0, 0),
            listOf(6, 0, 0, 1, 9, 5, 0, 0, 0),
            listOf(0, 9, 8, 0, 0, 0, 0, 6, 0),
            listOf(8, 0, 0, 0, 6, 0, 0, 0, 3),
            listOf(4, 0, 0, 8, 0, 3, 0, 0, 1),
            listOf(7, 0, 0, 0, 2, 0, 0, 0, 6),
            listOf(0, 6, 0, 0, 0, 0, 2, 8, 0),
            listOf(0, 0, 0, 4, 1, 9, 0, 0, 5),
            listOf(0, 0, 0, 0, 8, 0, 0, 7, 9)
        )

        coroutineScope {
            sudokuBuildUseCase(matrix, this) {
                when (it) {
                    is UseCase.Result.Error -> when (it.e) {
                    }

                    is UseCase.Result.Exception -> throw it.t
                    is UseCase.Result.Success -> {
                        stage = it.data
                    }
                }
            }
        }


        assertNotNull(stage)
        assertEquals(9, stage.rowCount)
        assertEquals(0, stage.getDuplicatedCellCount())
        assertTrue(stage.getCell(0, 8).isEmpty())
        assertTrue(stage.getCell(0, 0).isImmutable())
        assertEquals(5, stage[0, 0])
    }
}