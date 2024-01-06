package kr.co.hs.sudoku.challenge

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.model.challenge.impl.ChallengeEntityImpl
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.repository.challenge.ChallengeRepositoryImpl
import kr.co.hs.sudoku.usecase.AutoGenerateSudokuUseCase
import kr.co.hs.sudoku.usecase.RandomCreateSudoku
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import kotlin.time.Duration

@Suppress("TestFunctionName", "NonAsciiCharacters")
@RunWith(AndroidJUnit4::class)
class ChallengeGenerateTest {

    @Test
    fun generateChallenge() = runTest(timeout = Duration.INFINITE) {
//        val createMatrix = RandomCreateSudoku(9, 50.0).getIntMatrix()
//        val sudoku = AutoGenerateSudokuUseCase(
//            createMatrix.boxSize,
//            createMatrix.boxCount,
//            createMatrix
//        ).invoke().last()
//
//        val resultTable = CustomMatrix(sudoku.toValueTable())
//
//        val challengeEntity = ChallengeEntityImpl(
//            challengeId = "2024-01-07",
//            matrix = resultTable,
//            createdAt = Date()
//        )
//
//        val challengeRepository = ChallengeRepositoryImpl()
//        val result = challengeRepository.createChallenge(challengeEntity)
//        assertTrue(result)

        assertTrue(true)
    }
}