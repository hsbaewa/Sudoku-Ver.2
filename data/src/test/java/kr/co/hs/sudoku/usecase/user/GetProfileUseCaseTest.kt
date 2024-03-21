package kr.co.hs.sudoku.usecase.user

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.FirebaseTest
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.usecase.UseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
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
class GetProfileUseCaseTest : FirebaseTest() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var usecase: GetProfileUseCase

    @Inject
    lateinit var profileRepository: ProfileRepository

    override fun onBefore() {
        super.onBefore()
        hiltRule.inject()
        runTest(timeout = Duration.INFINITE) {
            profileRepository.setProfile(
                ProfileEntityImpl("uid-for-get-profile", "profile display name")
            )
        }
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        assertThrows(ProfileRepository.ProfileException.ProfileNotFound::class.java) {
            runBlocking { usecase("unknown-uid", this) }
        }

        assertNotNull(usecase("uid-for-get-profile", this))

        coroutineScope {
            usecase("unknown-uid", this) {
                when (it) {
                    is UseCase.Result.Error -> when (it.e) {
                        GetProfileUseCase.EmptyUserId -> throw Exception("EmptyUserId")
                        GetProfileUseCase.ProfileNotFound -> {}
                    }

                    is UseCase.Result.Exception -> throw it.t
                    is UseCase.Result.Success -> throw Exception("is not success")
                }
            }
        }

        coroutineScope {
            usecase("uid-for-get-profile", this) {
                when (it) {
                    is UseCase.Result.Error -> when (it.e) {
                        GetProfileUseCase.EmptyUserId -> throw Exception("EmptyUserId")
                        GetProfileUseCase.ProfileNotFound -> throw Exception("ProfileNotFound")
                    }

                    is UseCase.Result.Exception -> throw it.t
                    is UseCase.Result.Success -> assertEquals(
                        "profile display name",
                        it.data.displayName
                    )
                }
            }
        }
    }
}