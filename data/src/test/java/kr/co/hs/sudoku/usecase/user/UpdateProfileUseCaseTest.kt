package kr.co.hs.sudoku.usecase.user

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.FirebaseTest
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.usecase.UseCase
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
class UpdateProfileUseCaseTest : FirebaseTest() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var usecase: UpdateProfileUseCase

    @Inject
    lateinit var repository: ProfileRepository

    override fun onBefore() {
        super.onBefore()
        hiltRule.inject()
        runTest(timeout = Duration.INFINITE) {
            repository.setProfile(
                ProfileEntityImpl("uid-for-update", "display name")
                    .apply {
                        message = "message"
                        iconUrl = "https://any.image"
                    }
            )
        }
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        assertThrows(ProfileRepository.ProfileException.ProfileNotFound::class.java) {
            runBlocking { usecase(ProfileEntityImpl("uid-unknown", ""), this) }
        }
        assertThrows(ProfileRepository.ProfileException.EmptyUserId::class.java) {
            runBlocking { usecase(ProfileEntityImpl("", ""), this) }
        }

        var entity: ProfileEntity = ProfileEntityImpl("uid-for-update", "update name")
        entity = usecase(entity, this)
        assertEquals("uid-for-update", entity.uid)
        assertEquals("update name", entity.displayName)
        assertEquals("message", entity.message)
        assertEquals("https://any.image", entity.iconUrl)

        coroutineScope {
            usecase(ProfileEntityImpl("uid-for-update", "update name 2"), this) {
                when (it) {
                    is UseCase.Result.Error -> when (it.e) {
                        UpdateProfileUseCase.EmptyUserId -> throw Exception("EmptyUserId")
                        UpdateProfileUseCase.ProfileNotFound -> throw Exception("ProfileNotFound")
                    }

                    is UseCase.Result.Exception -> throw it.t
                    is UseCase.Result.Success -> entity = it.data
                }
            }
        }

        coroutineScope {
            usecase(ProfileEntityImpl("uid-unknown", ""), this) {
                when (it) {
                    is UseCase.Result.Error -> when (it.e) {
                        UpdateProfileUseCase.EmptyUserId -> throw Exception("EmptyUserId")
                        UpdateProfileUseCase.ProfileNotFound -> {}
                    }

                    is UseCase.Result.Exception -> throw it.t
                    is UseCase.Result.Success -> throw Exception("is not success")
                }
            }
        }

        coroutineScope {
            usecase(ProfileEntityImpl("", ""), this) {
                when (it) {
                    is UseCase.Result.Error -> when (it.e) {
                        UpdateProfileUseCase.EmptyUserId -> {}
                        UpdateProfileUseCase.ProfileNotFound -> throw Exception("ProfileNotFound")
                    }

                    is UseCase.Result.Exception -> throw it.t
                    is UseCase.Result.Success -> throw Exception("is not success")
                }
            }
        }
    }
}