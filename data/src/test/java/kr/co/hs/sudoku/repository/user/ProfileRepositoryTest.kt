package kr.co.hs.sudoku.repository.user

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.FirebaseTest
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.time.Duration

@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class ProfileRepositoryTest : FirebaseTest() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun before() = hiltRule.inject()

    @Inject
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
            runBlocking { profileRepository.checkIn("") }
        }
        assertThrows(Exception::class.java) {
            runBlocking { profileRepository.checkIn("asdasdasdasdasd") }
        }
        assertThrows(Exception::class.java) {
            runBlocking { profileRepository.checkIn("") }
        }
        assertThrows(Exception::class.java) {
            runBlocking { profileRepository.checkIn("asdassdasdasfadsadas") }
        }
        dummyProfile = profileRepository.checkIn(dummyProfile.uid)
        assertTrue(dummyProfile is ProfileEntity.OnlineUserEntity)

        var onlineUserList = profileRepository.runCatching { getOnlineUserList() }.getOrNull()
        assertEquals(true, onlineUserList?.find { it.uid == "uid-dummy" } != null)

        dummyProfile = profileRepository.checkOut(dummyProfile.uid)
        assertTrue(dummyProfile is ProfileEntity.UserEntity)

        onlineUserList = profileRepository.runCatching { getOnlineUserList() }.getOrNull()
        assertEquals(true, onlineUserList?.find { it.uid == "uid-dummy" } == null)
    }
}