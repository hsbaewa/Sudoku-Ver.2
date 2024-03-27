package kr.co.hs.sudoku.usecase.user

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.data.TestProfileDataSource
import kr.co.hs.sudoku.repository.user.TestProfileRepository
import kr.co.hs.sudoku.usecase.NoErrorUseCase
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
                usecase().collect {
                    when (it) {
                        is NoErrorUseCase.Result.Exception -> throw it.t
                        is NoErrorUseCase.Result.Success -> {}
                    }
                }
            }
        }

        assertThrows(Exception::class.java) {
            runBlocking { usecase().first() }
        }
    }
}