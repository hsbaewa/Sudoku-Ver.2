package kr.co.hs.sudoku.usecase.user

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.data.TestProfileDataSource
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.repository.user.TestProfileRepository
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckOutUseCaseTest {
    private lateinit var repository: ProfileRepository
    private lateinit var usecase: CheckOutUseCase
    private var existProfile: ProfileEntity = ProfileEntityImpl("", "")

    @Before
    fun before() = runTest {
        repository = TestProfileRepository(TestProfileDataSource())
        usecase = CheckOutUseCase(repository)
        existProfile = repository.getOnlineUserList().first()
    }

    @Test
    fun do_test() = runTest {
        Assert.assertThrows(ProfileRepository.ProfileException.EmptyUserId::class.java) {
            runBlocking { usecase(ProfileEntityImpl("", ""), this) }
        }

        Assert.assertThrows(ProfileRepository.ProfileException.ProfileNotFound::class.java) {
            runBlocking { usecase(ProfileEntityImpl("no uid", ""), this) }
        }

        assertTrue(existProfile is ProfileEntity.OnlineUserEntity)
        usecase(existProfile, this)
        existProfile = repository.getProfile(existProfile.uid)
        assertTrue(existProfile is ProfileEntity.UserEntity)
    }
}