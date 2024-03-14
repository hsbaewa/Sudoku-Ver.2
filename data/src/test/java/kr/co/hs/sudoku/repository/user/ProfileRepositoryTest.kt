package kr.co.hs.sudoku.repository.user

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.FirebaseTest
import kr.co.hs.sudoku.di.ProfileRepositoryQualifier
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
        val onlineUserList = profileRepository.runCatching { getOnlineUserList() }.getOrNull()
        assertNotNull(onlineUserList)
    }
}