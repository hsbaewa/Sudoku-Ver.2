package kr.co.hs.sudoku.usecase.user

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.FirebaseTest
import kr.co.hs.sudoku.di.ProfileRepositoryQualifier
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl
import kr.co.hs.sudoku.repository.user.ProfileRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.time.Duration

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class GetOnlineProfileListUseCaseTest : FirebaseTest() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var usecase: GetOnlineProfileListUseCase

    @Inject
    @ProfileRepositoryQualifier
    lateinit var profileRepository: ProfileRepository

    @Before
    fun before() = runTest(timeout = Duration.INFINITE) {
        hiltRule.inject()
        profileRepository.setProfile(ProfileEntityImpl("uid-1", "displayName-1"))
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        assertNull(usecase(this).find { it.uid == "uid-1" })

        profileRepository.checkIn("uid-1")
        assertNotNull(usecase(this).find { it.uid == "uid-1" })

        profileRepository.checkOut("uid-1")
        assertNull(usecase(this).find { it.uid == "uid-1" })
    }
}