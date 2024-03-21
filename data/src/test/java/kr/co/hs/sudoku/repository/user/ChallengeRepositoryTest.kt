package kr.co.hs.sudoku.repository.user

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.FirebaseTest
import kr.co.hs.sudoku.feature.user.Authenticator
import kr.co.hs.sudoku.model.challenge.impl.ChallengeEntityImpl
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.usecase.SudokuGenerateUseCase
import kr.co.hs.sudoku.usecase.SudokuRandomGenerateUseCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date
import javax.inject.Inject
import kotlin.time.Duration

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class ChallengeRepositoryTest : FirebaseTest() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var authenticator: Authenticator

    @Inject
    lateinit var challengeRepository: ChallengeRepository

    @Inject
    lateinit var sudokuGenerator: SudokuGenerateUseCase

    @Before
    fun before() = runTest(timeout = Duration.INFINITE) {
        hiltRule.inject()
        authenticator.signIn().first()
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
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


//        val profile = authenticator.getProfile().firstOrNull() ?: throw Exception("sign first")

//        val history = challengeRepository.getHistory(profile.uid, 50)
//
//        assertEquals(true, history.isNotEmpty())
//        assertEquals(profile.uid, history.first().uid)
//        assertEquals(true, history.first().challengeId.isNotEmpty())
//        assertEquals(1, history.first().grade)
//        assertEquals(20000, history.first().record.toInt())
    }
}