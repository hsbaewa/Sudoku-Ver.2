package kr.co.hs.sudoku.usecase.user

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.repository.user.TestProfileRepository
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration

class CheckInUseCaseTest {
    private lateinit var repository: ProfileRepository
    private lateinit var usecase: CheckInUseCase
    private var existProfile: ProfileEntity? = null

    @Before
    fun before() = runTest {
        repository = TestProfileRepository()
        usecase = CheckInUseCase(repository)
        existProfile = repository.getProfile("0")
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        assertThrows(ProfileRepository.ProfileException.EmptyUserId::class.java) {
            runBlocking { usecase(ProfileEntityImpl("", ""), this) }
        }

        assertThrows(ProfileRepository.ProfileException.ProfileNotFound::class.java) {
            runBlocking { usecase(ProfileEntityImpl("no uid", ""), this) }
        }

        usecase(existProfile!!, this)
        existProfile = repository.getProfile(existProfile!!.uid)
        assertTrue(existProfile is ProfileEntity.OnlineUserEntity)
    }
}