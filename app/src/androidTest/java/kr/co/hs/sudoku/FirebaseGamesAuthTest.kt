package kr.co.hs.sudoku

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kr.co.hs.sudoku.model.challenge.impl.ChallengeEntityImpl
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.repository.challenge.ChallengeRepositoryImpl
import kr.co.hs.sudoku.usecase.BuildSudokuUseCaseImpl
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
//    @Test
//    fun useAppContext() = runBlocking {
//        // Context of the app under test.
//        val scenario = ActivityScenario.launch(AuthTestActivity::class.java)
//
//        delay(5000)
//
//        scenario.onActivity { activity ->
//            val text = activity.findViewById<TextView>(R.id.tvInfo).text.toString()
//            assertEquals("제르체프", text)
//        }
//
//        return@runBlocking
//
//    }

    @Test
    fun getChallengeIds() = runBlocking {
        val repository = ChallengeRepositoryImpl()
        val getChallengeIds = GetChallengeListUseCaseImpl(repository).invoke().toList()
        assertTrue(getChallengeIds.isNotEmpty())

        return@runBlocking
    }

    @Test
    fun createChallenge() = runBlocking {
        val challengeEntity = ChallengeEntityImpl(
            "2023-04-24",
            /*
            1 4 3 2
2 3 1 4
4 1 2 3
3 2 4 0

             */
            CustomMatrix(
                listOf(
                    listOf(1,4,3,2),
                    listOf(2,3,1,4),
                    listOf(4,1,2,3),
                    listOf(3,2,4,0)
                )
            )
//            CustomMatrix(
//                listOf(
//                    listOf(5, 3, 0, 0, 7, 0, 0, 0, 0),
//                    listOf(6, 0, 0, 1, 9, 5, 0, 0, 0),
//                    listOf(0, 9, 8, 0, 0, 0, 0, 6, 0),
//                    listOf(8, 0, 0, 0, 6, 0, 0, 0, 3),
//                    listOf(4, 0, 0, 8, 0, 3, 0, 0, 1),
//                    listOf(7, 0, 0, 0, 2, 0, 0, 0, 6),
//                    listOf(0, 6, 0, 0, 0, 0, 2, 8, 0),
//                    listOf(0, 0, 0, 4, 1, 9, 0, 0, 5),
//                    listOf(0, 0, 0, 0, 8, 0, 0, 7, 9)
//                )
//            )
        )

        val repository = ChallengeRepositoryImpl()
        repository.createChallenge(challengeEntity)

        val result = repository.getLatestChallenge()
        val sudoku = BuildSudokuUseCaseImpl(result.matrix).invoke().last()
        assertNotNull(sudoku)

        return@runBlocking
    }


}