package kr.co.hs.sudoku.usecase.user

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.data.TestProfileDataSource
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.repository.user.TestProfileRepository
import kr.co.hs.sudoku.usecase.UseCase
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration

class GetOnlineProfileListUseCaseTest {
    private lateinit var testRepository: ProfileRepository
    private lateinit var usecase: GetOnlineProfileListUseCase

    @Before
    fun before() {
        testRepository = TestProfileRepository(TestProfileDataSource())
        usecase = GetOnlineProfileListUseCase(testRepository)
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        val result = usecase(this)
        assertNotNull(result)
        assertEquals(1, result.size)

        coroutineScope {
            usecase(this) {
                when (it) {
                    is UseCase.Result.Error -> throw Exception("invalid error")
                    is UseCase.Result.Exception -> throw it.t
                    is UseCase.Result.Success -> {
                        assertNotNull(it.data)
                        assertEquals(1, it.data.size)
                    }
                }
            }
        }
    }
}