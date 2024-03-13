package kr.co.hs.sudoku.challenge

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.di.repositories.ChallengeRepositoryQualifier
import kr.co.hs.sudoku.model.challenge.impl.ChallengeEntityImpl
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.repository.TestableRepository
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.usecase.SudokuGenerateUseCase
import kr.co.hs.sudoku.usecase.SudokuRandomGenerateUseCase
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import javax.inject.Inject
import kotlin.time.Duration

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ChallengeListTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    @ChallengeRepositoryQualifier
    lateinit var challengeRepository: ChallengeRepository

    @Inject
    lateinit var sudokuGenerator: SudokuGenerateUseCase

    @Before
    fun before() {
        hiltRule.inject()
    }

    @Test
    fun getChallengeListTest() = runTest(timeout = Duration.INFINITE) {
        val challengeRepository = spyk(challengeRepository, recordPrivateCalls = true)
        (challengeRepository as TestableRepository).setFireStoreRootVersion("test")
        every { challengeRepository.getProperty("currentUserUid") } returns "lYYBEGzX9JggNlChJs7C9OPtVe82"

        val stage = sudokuGenerator(SudokuRandomGenerateUseCase.Param(9, 50.0), this)
        val matrix = CustomMatrix(stage.toValueTable())
        val challengeEntity = ChallengeEntityImpl(matrix)

        challengeRepository.createChallenge(challengeEntity)

        var list =
            challengeRepository.getChallenges(Date(System.currentTimeMillis() + 60 * 1000), 10000)
        val listSize = list.size

        assertEquals(true, listSize > 0)

        assertEquals(list.first().matrix, challengeEntity.matrix)

        val result = challengeRepository.putRecord(list.first().challengeId, 20000)
        assertEquals(true, result)

        val record = challengeRepository.getRecords(list.first().challengeId).firstOrNull()
        assertEquals(20000, record?.clearTime?.toInt())

        list = challengeRepository.getChallenges(Date(), 10000)
        assertEquals(listSize, list.size)

        val history = challengeRepository.getHistory("lYYBEGzX9JggNlChJs7C9OPtVe82", 50)

        assertEquals(true, history.isNotEmpty())
        assertEquals("lYYBEGzX9JggNlChJs7C9OPtVe82", history.first().uid)
        assertEquals(true, history.first().challengeId.isNotEmpty())
        assertEquals(1, history.first().grade)
        assertEquals(20000, history.first().record.toInt())
    }
}