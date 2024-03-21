package kr.co.hs.sudoku.usecase.user

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.data.TestProfileDataSource
import kr.co.hs.sudoku.repository.user.TestProfileRepository
import kr.co.hs.sudoku.usecase.UseCase
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration

class GetCurrentUserProfileUseCaseTest {

    private lateinit var usecase: GetCurrentUserProfileUseCase

    @Before
    fun before() {
        usecase = GetCurrentUserProfileUseCase(
            TestProfileRepository(TestProfileDataSource())
        )
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        assertThrows(Exception::class.java) {
            runBlocking {
                usecase(this) {
                    when (it) {
                        is UseCase.Result.Error -> when (it.e) {
                            GetCurrentUserProfileUseCase.NotExistCurrentUser -> throw Exception("NotExistCurrentUser")
                        }

                        is UseCase.Result.Exception -> throw it.t
                        is UseCase.Result.Success -> {}
                    }
                }
            }
        }

        assertThrows(GetCurrentUserProfileUseCase.NotExistCurrentUserException::class.java) {
            runBlocking { usecase(this) }
        }
    }
}