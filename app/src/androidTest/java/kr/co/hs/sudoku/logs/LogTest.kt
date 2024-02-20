package kr.co.hs.sudoku.logs

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.datasource.logs.LogRemoteSource
import kr.co.hs.sudoku.datasource.logs.impl.LogRemoteSourceImpl
import kr.co.hs.sudoku.model.logs.impl.BattleClearModelImpl
import kr.co.hs.sudoku.model.logs.impl.ChallengeClearModelImpl
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import kotlin.time.Duration

@Suppress("TestFunctionName", "NonAsciiCharacters")
@RunWith(AndroidJUnit4::class)
class LogTest {

    private lateinit var logRemoteSource: LogRemoteSource

    @Before
    fun 테스트_초기화() {
        logRemoteSource = LogRemoteSourceImpl()
            .apply {
                rootDocument = FirebaseFirestore.getInstance()
                    .collection("version")
                    .document("test")
            }
    }

    @Test
    fun 로그_생성_테스트() = runTest(timeout = Duration.INFINITE) {
        logRemoteSource.createLog(ChallengeClearModelImpl()
            .apply {
                uid = "test-user-1"
                challengeId = "challenge-1"
                record = 10
            }
        )
        logRemoteSource.createLog(ChallengeClearModelImpl()
            .apply {
                uid = "test-user-2"
                challengeId = "challenge-1"
                record = 100
            }
        )
        logRemoteSource.createLog(BattleClearModelImpl()
            .apply {
                uid = "test-user-1"
                battleWith = listOf("other1", "other2")
                record = 100
                battleId = "battle1"
            }
        )
    }

    @Test
    fun 로그_조회_테스트() = runTest(timeout = Duration.INFINITE) {
        logRemoteSource.createLog(BattleClearModelImpl()
            .apply {
                uid = "test-user-1"
                battleWith = listOf("other1", "other2")
                record = 100
                battleId = "battle1"
            }
        )

        logRemoteSource.createLog(BattleClearModelImpl()
            .apply {
                uid = "test-user-2"
                battleWith = listOf("other1", "other2")
                record = 10
                battleId = "battle1"
            }
        )

        val list =
            logRemoteSource.getLogs("test-user-1", Date(System.currentTimeMillis() + 60 * 1000), 10)
        assertEquals(true, list.isNotEmpty())

        val list2 =
            logRemoteSource.getLogs("any-one", Date(System.currentTimeMillis() + 60 * 1000), 10)
        assertEquals(true, list2.isEmpty())

        val list3 =
            logRemoteSource.getLogs(Date(System.currentTimeMillis() + 60 * 1000), 10)
        assertEquals(true, list3.isNotEmpty())
        assertEquals(true, list3.find { it.uid == "test-user-1" } != null)
        assertEquals(true, list3.first().uid == "test-user-2")
    }

    @After
    fun 로그_삭제() = runTest(timeout = Duration.INFINITE) {
        val list = logRemoteSource.getLogs(Date(System.currentTimeMillis() + 60 * 1000), 100)
        list.forEach {
            logRemoteSource.deleteLog(it.id)
        }
    }

}