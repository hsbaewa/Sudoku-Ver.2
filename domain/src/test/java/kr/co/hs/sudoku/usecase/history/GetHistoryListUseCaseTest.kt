package kr.co.hs.sudoku.usecase.history

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.data.TestHistoryDataSource
import kr.co.hs.sudoku.data.TestProfileDataSource
import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.history.HistoryRepository
import kr.co.hs.sudoku.repository.history.MyHistoryRepository
import kr.co.hs.sudoku.repository.history.TestHistoryRepository
import kr.co.hs.sudoku.repository.history.TestMyHistoryRepository
import kr.co.hs.sudoku.repository.user.TestProfileRepository
import kr.co.hs.sudoku.usecase.user.GetCurrentUserProfileUseCase
import org.junit.Before
import org.junit.Test
import java.util.Date
import kotlin.time.Duration

class GetHistoryListUseCaseTest {
    private lateinit var historyRepository: HistoryRepository
    private lateinit var myHistoryRepository: MyHistoryRepository
    private lateinit var useCase: GetHistoryListUseCase
    private lateinit var getCurrentUserProfile: GetCurrentUserProfileUseCase

    @Before
    fun before() {
        val dataSource = TestHistoryDataSource()
        historyRepository = TestHistoryRepository(dataSource)
        myHistoryRepository = TestMyHistoryRepository(
            dataSource,
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
        getCurrentUserProfile = GetCurrentUserProfileUseCase(
            TestProfileRepository(TestProfileDataSource())
        )
        useCase = GetHistoryListUseCase(historyRepository, myHistoryRepository)
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        val historyList = useCase(Date(), 1).toList()
        assertEquals(1, historyList.size)
    }
}