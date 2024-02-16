package kr.co.hs.sudoku.challenge

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.repository.challenge.ChallengeRepositoryImpl
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import kotlin.time.Duration

@RunWith(AndroidJUnit4::class)
class ChallengeListTest {
    @Test
    fun getChallengeListTest() = runTest(timeout = Duration.INFINITE) {
        val challengeRepository2 = spyk<ChallengeRepositoryImpl>(recordPrivateCalls = true)
        challengeRepository2.setFireStoreRootVersion("test")
        every { challengeRepository2.getProperty("currentUserUid") } returns "lYYBEGzX9JggNlChJs7C9OPtVe82"

        val page1 = challengeRepository2.getChallenges(Date(), 10)
        assertEquals(10, page1.size)

        val page2 = challengeRepository2.getChallenges(page1.last().createdAt!!, 10)
        assertEquals(page1.last(), page2.first())
    }
}