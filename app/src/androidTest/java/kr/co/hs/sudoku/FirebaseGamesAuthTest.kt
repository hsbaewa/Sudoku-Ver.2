package kr.co.hs.sudoku

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kr.co.hs.sudoku.repository.TestableRepository
import kr.co.hs.sudoku.repository.challenge.ChallengeRepositoryImpl
import kr.co.hs.sudoku.usecase.challenge.GetChallengeListUseCaseImpl
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Test

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class FirebaseGamesAuthTest {

    @Test
    fun getChallengeIds() = runBlocking {
        val repository = ChallengeRepositoryImpl()
        (repository as TestableRepository).setFireStoreRootVersion("test")
        val getChallengeIds = GetChallengeListUseCaseImpl(repository).invoke().toList()
        assertTrue(getChallengeIds.isNotEmpty())

        return@runBlocking
    }


}