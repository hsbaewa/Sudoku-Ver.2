package kr.co.hs.sudoku.usecase.history

import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.data.TestChallengeDataSource
import kr.co.hs.sudoku.data.TestHistoryDataSource
import kr.co.hs.sudoku.data.TestRecordDataSource
import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.repository.challenge.TestChallengeRepository
import kr.co.hs.sudoku.repository.history.MyHistoryRepository
import kr.co.hs.sudoku.repository.history.TestMyHistoryRepository
import kr.co.hs.sudoku.usecase.UseCaseFlow
import org.junit.Before
import org.junit.Test
import java.util.Date
import kotlin.time.Duration

class CreateHistoryUseCaseTest {

    private lateinit var challengeRepository: ChallengeRepository
    private lateinit var myHistoryRepository: MyHistoryRepository
    private lateinit var usecase: CreateHistoryUseCase

    @Before
    fun before() {
        challengeRepository =
            TestChallengeRepository(TestChallengeDataSource(), TestRecordDataSource())
        myHistoryRepository = TestMyHistoryRepository(
            TestHistoryDataSource(),
            getCurrentUserProfile = {
                flow {
                    emit(
                        object : ProfileEntity.UserEntity {
                            override val lastCheckedAt: Date? = null
                            override val uid: String = "uid-dummy"
                            override var displayName: String = ""
                            override var message: String? = null
                            override var iconUrl: String? = null
                            override val locale: LocaleEntity? = null
                        }
                    )
                }
            }
        )
        usecase = CreateHistoryUseCase(myHistoryRepository)
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        val challenge = challengeRepository.getChallenges(Date(), 1).first()
        usecase(challenge, 2000).collect {
            when (it) {
                is UseCaseFlow.Result.Error -> throw Exception("error")
                is UseCaseFlow.Result.Exception -> throw it.t
                is UseCaseFlow.Result.Success -> {
                    assertNotNull(it.data)
                }
            }
        }
    }
}