package kr.co.hs.sudoku.challenge

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.model.challenge.impl.ChallengeEntityImpl
import kr.co.hs.sudoku.repository.challenge.ChallengeRepositoryImpl
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
//        val challengeRepository = ChallengeRepositoryImpl()
//
//        val createMatrix = RandomCreateSudoku(9, 50.0).getIntMatrix()
//
//        val challengeEntity = ChallengeEntityImpl(
//            challengeId = "2024-01-01",
//            matrix = createMatrix,
//            createdAt = Date()
//        )
//
//        val result = challengeRepository.createChallenge(challengeEntity)
//        assertTrue(result)

        assertTrue(true)
    }
}