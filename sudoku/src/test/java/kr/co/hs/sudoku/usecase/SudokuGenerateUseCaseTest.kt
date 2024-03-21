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

class SudokuGenerateUseCaseTest {
    private lateinit var sudokuGenerateUseCase: SudokuGenerateUseCase

    @Before
    fun before() {
        sudokuGenerateUseCase = SudokuGenerateUseCase()
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        var matrix = listOf(
            listOf(1, 0, 0, 1),
            listOf(0, 1, 1, 0),
            listOf(1, 0, 0, 1),
            listOf(0, 1, 1, 0)
        )

        var stage = sudokuGenerateUseCase(matrix, this)

        assertNotNull(stage)
        assertEquals(4, stage.rowCount)
        assertTrue(stage.getDuplicatedCells().isEmpty())


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
        stage = sudokuGenerateUseCase(matrix, this)
        assertEquals(9, stage.rowCount)
        assertTrue(stage.getDuplicatedCells().isEmpty())


        matrix = listOf(
            listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
            listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
            listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
            listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
            listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
            listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
            listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
            listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
            listOf(1, 1, 1, 1, 1, 1, 1, 1, 1)
        )

        coroutineScope {
            sudokuGenerateUseCase(matrix, this) {
                when (it) {
                    is UseCase.Result.Error -> when (it.e) {
                    }

                    is UseCase.Result.Exception -> throw it.t
                    is UseCase.Result.Success -> stage = it.data
                }
            }
        }

        assertTrue(stage.getDuplicatedCells().isEmpty())
        assertTrue(stage.isSudokuClear())
    }
}