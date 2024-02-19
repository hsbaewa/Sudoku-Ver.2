package kr.co.hs.sudoku.challenge

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.model.challenge.impl.ChallengeEntityImpl
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.repository.challenge.ChallengeRepositoryImpl
import kr.co.hs.sudoku.usecase.AutoGenerateSudokuUseCase
import kr.co.hs.sudoku.usecase.RandomCreateSudoku
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import kotlin.time.Duration

@RunWith(AndroidJUnit4::class)
class ChallengeListTest {
    @Test
    fun getChallengeListTest() = runTest(timeout = Duration.INFINITE) {
        val challengeRepository = spyk<ChallengeRepositoryImpl>(recordPrivateCalls = true)
        challengeRepository.setFireStoreRootVersion("test")
        every { challengeRepository.getProperty("currentUserUid") } returns "lYYBEGzX9JggNlChJs7C9OPtVe82"

        val matrix = RandomCreateSudoku(9, 50.0)
            .getIntMatrix()
            .run { AutoGenerateSudokuUseCase(boxSize, boxCount, this).invoke().last() }
            .run { CustomMatrix(this.toValueTable()) }
        val challengeEntity = ChallengeEntityImpl(matrix)

        challengeRepository.createChallenge(challengeEntity)

        delay(5000)

        var list = challengeRepository.getChallenges(Date(), 10000)
        val listSize = list.size

        assertEquals(true, listSize > 0)

        assertEquals(list.first().matrix, challengeEntity.matrix)

        challengeRepository.removeChallenge(list.first().challengeId)

        list = challengeRepository.getChallenges(Date(), 10000)
        assertEquals(listSize - 1, list.size)
    }
}