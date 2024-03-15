package kr.co.hs.sudoku.repository.user

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.FirebaseTest
import kr.co.hs.sudoku.di.ProfileRepositoryQualifier
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.time.Duration

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class ProfileRepositoryTest : FirebaseTest() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun before() = hiltRule.inject()

    @Inject
    @ProfileRepositoryQualifier
    lateinit var profileRepository: ProfileRepository

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        var dummyProfile: ProfileEntity = ProfileEntityImpl("uid-dummy", "display name for dummy")

        assertThrows(Exception::class.java) {
            runBlocking { profileRepository.setProfile(ProfileEntityImpl("", "")) }
        }

        profileRepository.setProfile(dummyProfile)

        assertThrows(Exception::class.java) {
            runBlocking { profileRepository.getProfile("") }
        }
        assertThrows(Exception::class.java) {
            runBlocking { profileRepository.getProfile("asdasdasdasd") }
        }

        dummyProfile = profileRepository.getProfile(dummyProfile.uid)
        assertTrue(dummyProfile is ProfileEntity.UserEntity)

        assertThrows(Exception::class.java) {
            runBlocking { profileRepository.checkIn(ProfileEntityImpl("", "")) }
        }
        assertThrows(Exception::class.java) {
            runBlocking { profileRepository.checkIn(ProfileEntityImpl("asdasdasdasdasd", "")) }
        }
        assertThrows(Exception::class.java) {
            runBlocking { profileRepository.checkIn("") }
        }
        assertThrows(Exception::class.java) {
            runBlocking { profileRepository.checkIn("asdassdasdasfadsadas") }
        }
        profileRepository.checkIn(dummyProfile.uid)
        dummyProfile = profileRepository.getProfile(dummyProfile.uid)
        assertTrue(dummyProfile is ProfileEntity.OnlineUserEntity)
        profileRepository.checkIn(dummyProfile)
        assertTrue(dummyProfile is ProfileEntity.OnlineUserEntity)

        var onlineUserList = profileRepository.runCatching { getOnlineUserList() }.getOrNull()
        assertEquals(true, onlineUserList?.isNotEmpty())

        profileRepository.checkOut(dummyProfile.uid)
        dummyProfile = profileRepository.getProfile(dummyProfile.uid)
        assertTrue(dummyProfile is ProfileEntity.UserEntity)

        onlineUserList = profileRepository.runCatching { getOnlineUserList() }.getOrNull()
        assertEquals(true, onlineUserList?.isEmpty())
    }
}