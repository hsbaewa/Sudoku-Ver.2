package kr.co.hs.sudoku.usecase.user

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.data.TestProfileDataSource
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.repository.user.TestProfileRepository
import kr.co.hs.sudoku.usecase.UseCase
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration

class GetProfileUseCaseTest {
    private lateinit var testRepository: ProfileRepository
    private lateinit var getProfileUseCase: GetProfileUseCase

    @Before
    fun before() {
        testRepository = TestProfileRepository(TestProfileDataSource())
        getProfileUseCase = GetProfileUseCase(testRepository)
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        assertThrows(
            "ProfileNotFound",
            Exception::class.java
        ) {
            runBlocking {
                getProfileUseCase("any uid", this) {
                    when (it) {
                        is UseCase.Result.Error -> when (it.e) {
                            GetProfileUseCase.EmptyUserId -> throw Exception("EmptyUserId")
                            GetProfileUseCase.ProfileNotFound -> throw Exception("ProfileNotFound")
                        }

                        is UseCase.Result.Exception -> throw it.t
                        is UseCase.Result.Success -> {}
                    }
                }
            }
        }

        coroutineScope {
            getProfileUseCase("0", this) {
                when (it) {
                    is UseCase.Result.Error -> throw Exception("error")
                    is UseCase.Result.Exception -> throw it.t
                    is UseCase.Result.Success -> {
                        assertEquals("0", it.data.uid)
                    }
                }
            }
        }

    }
}