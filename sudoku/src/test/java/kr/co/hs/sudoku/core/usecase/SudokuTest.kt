package kr.co.hs.sudoku.core.usecase

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.usecase.SudokuBuildUseCase
import kr.co.hs.sudoku.usecase.SudokuGenerateUseCase
import kr.co.hs.sudoku.usecase.SudokuRandomGenerateUseCase
import org.junit.Test
import kotlin.time.Duration

class SudokuTest {
    @Test
    fun 빌드_테스트() = runTest(timeout = Duration.INFINITE) {
        val usecase = SudokuBuildUseCase()
        val stage = usecase(
            listOf(
                listOf(3, 4, 1, 2),
                listOf(0, 1, 4, 0),
                listOf(1, 0, 0, 4),
                listOf(0, 3, 2, 0)
            ), this
        )

        assertNotNull(stage)

        assertEquals(4, stage.rowCount)

        assertTrue(stage.getDuplicatedCells().isEmpty())
    }

    @Test
    fun 생성_테스트() = runTest(timeout = Duration.INFINITE) {
        val usecase = SudokuGenerateUseCase()
        val stage = usecase(
            listOf(
                listOf(1, 0, 0, 1),
                listOf(0, 1, 1, 0),
                listOf(1, 0, 0, 1),
                listOf(0, 1, 1, 0)
            ), this
        )

        assertNotNull(stage)

        assertEquals(4, stage.rowCount)

        assertTrue(stage.getDuplicatedCells().isEmpty())
    }

    @Test
    fun 랜덤_테스트() = runTest(timeout = Duration.INFINITE) {
        val usecase = SudokuRandomGenerateUseCase()
        val stage = usecase(SudokuRandomGenerateUseCase.Param(4, 50.0), this)

        assertNotNull(stage)

        assertEquals(4, stage.rowCount)
    }
}