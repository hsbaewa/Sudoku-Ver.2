package kr.co.hs.sudoku.usecase.user

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.repository.user.TestProfileRepository
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration

class CreateProfileUseCaseTest {
    private lateinit var testRepository: ProfileRepository
    private lateinit var usecase: CreateProfileUseCase

    @Before
    fun before() {
        testRepository = TestProfileRepository()
        usecase = CreateProfileUseCase(testRepository)
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        val response = usecase(ProfileEntityImpl("1010", "1010"), this)
        assertEquals(response.uid, "1010")

        assertThrows(Exception::class.java) {
            runBlocking { usecase(ProfileEntityImpl("0", "0"), this) }
        }
    }
}