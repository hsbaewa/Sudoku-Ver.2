package kr.co.hs.sudoku

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.model.matrix.BeginnerMatrix
import kr.co.hs.sudoku.model.matrix.IntermediateMatrix
import kr.co.hs.sudoku.repository.stage.MatrixRepository
import kr.co.hs.sudoku.usecase.GetSudokuUseCaseImpl
import kr.co.hs.sudoku.usecase.PlaySudokuUseCaseImpl
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration

class StageUseCaseTest {
    @Test
    fun testBeginnerRepository() = runBlocking {
        val matrixRepo: MatrixRepository<BeginnerMatrix> = mockk()
        coEvery { matrixRepo.getList() } answers {
            listOf(
                BeginnerMatrix(
                    matrix = listOf(
                        listOf(0, 1, 1, 0),
                        listOf(1, 0, 0, 1),
                        listOf(1, 0, 0, 1),
                        listOf(0, 1, 1, 0)
                    )
                )
            )
        }

        val getSudokuUseCase = GetSudokuUseCaseImpl(matrixRepo)
        val stage = getSudokuUseCase(0).first().invoke().first()

        println(stage)
        assertEquals(false, stage.isSudokuClear())

        val playUseCase = PlaySudokuUseCaseImpl(stage, 300)
        playUseCase().collect {
            println(it)
            println(stage)
        }
        assertEquals(true, stage.isSudokuClear())
    }

    @Test
    fun testIntermediateRepository() = runTest(timeout = Duration.INFINITE) {
        val matrixRepo: MatrixRepository<IntermediateMatrix> = mockk()
        coEvery { matrixRepo.getList() } answers {
            listOf(
                IntermediateMatrix(
                    matrix = listOf(
                        listOf(0, 1, 1, 0, 1, 0, 1, 1, 0),
                        listOf(1, 1, 0, 1, 0, 1, 0, 1, 1),
                        listOf(0, 1, 0, 0, 1, 0, 0, 1, 0),
                        listOf(0, 1, 0, 1, 0, 1, 0, 1, 0),
                        listOf(1, 0, 1, 0, 1, 0, 1, 0, 1),
                        listOf(0, 1, 0, 1, 0, 1, 0, 1, 0),
                        listOf(0, 1, 0, 0, 1, 0, 0, 1, 0),
                        listOf(1, 1, 0, 1, 0, 1, 0, 1, 1),
                        listOf(0, 1, 1, 0, 1, 0, 1, 1, 0)
                    )
                )
            )
        }

        val getSudokuUseCase = GetSudokuUseCaseImpl(matrixRepo)
        val stage = getSudokuUseCase(0).first()().first()

        println(stage)
        assertEquals(false, stage.isSudokuClear())

        val playUseCase = PlaySudokuUseCaseImpl(stage, 0)
        playUseCase().collect()
        println(stage)
        assertEquals(true, stage.isSudokuClear())
    }

//    @Test
//    fun testIntermediatePlay() = runBlocking {
//        val matrixRepo: MatrixRepository<IntermediateMatrix> = mockk()
//        coEvery { matrixRepo.getList() } answers {
//            listOf(
//                IntermediateMatrix(
//                    matrix = listOf(
//                        listOf(0, 1, 1, 0, 1, 0, 1, 1, 0),
//                        listOf(1, 1, 0, 1, 0, 1, 0, 1, 1),
//                        listOf(0, 1, 0, 0, 1, 0, 0, 1, 0),
//                        listOf(0, 1, 0, 1, 0, 1, 0, 1, 0),
//                        listOf(1, 0, 1, 0, 1, 0, 1, 0, 1),
//                        listOf(0, 1, 0, 1, 0, 1, 0, 1, 0),
//                        listOf(0, 1, 0, 0, 1, 0, 0, 1, 0),
//                        listOf(1, 1, 0, 1, 0, 1, 0, 1, 1),
//                        listOf(0, 1, 1, 0, 1, 0, 1, 1, 0)
//                    )
//                )
//            )
//        }
//
//        val getSudokuUseCase = GetSudokuUseCaseImpl(matrixRepo)
//        val stage = getSudokuUseCase(0).first()().first()
//
//        println(stage)
//        assertEquals(false, stage.isSudokuClear())
//
//        val playUseCase = PlaySudokuUseCaseImpl(stage, 300)
//        playUseCase().collect {
//            println(it)
//            println(stage)
//        }
//        assertEquals(true, stage.isSudokuClear())
//    }

//    @Test
//    fun testAdvancedRepository() = runTest(timeout = Duration.INFINITE) {
//        val matrixRepo: MatrixRepository<AdvancedMatrix> = mockk()
//        coEvery { matrixRepo.getList() } answers {
//            listOf(
//                AdvancedMatrix(
//                    matrix = listOf(
//                        listOf(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0),
//                        listOf(0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0),
//                        listOf(0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0),
//                        listOf(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0),
//                        listOf(0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0),
//                        listOf(0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0),
//                        listOf(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0),
//                        listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
//                        listOf(0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0),
//                        listOf(0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0),
//                        listOf(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0),
//                        listOf(0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0),
//                        listOf(0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0),
//                        listOf(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0),
//                        listOf(0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0),
//                        listOf(0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0)
//                    )
//                )
//            )
//        }
//
//        val getSudokuUseCase = GetSudokuUseCaseImpl(matrixRepo)
//        val stage = getSudokuUseCase(0).first()().first()
//
//
//        println(stage)
//        assertEquals(false, stage.isSudokuClear())
//
//        val playUseCase = PlaySudokuUseCaseImpl(stage, 0)
//        playUseCase().collect()
//        println(stage)
//        assertEquals(true, stage.isSudokuClear())
//    }

}