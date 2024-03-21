package kr.co.hs.sudoku.usecase.user

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.FirebaseTest
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.usecase.UseCase
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
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
class CheckInUseCaseTest : FirebaseTest() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var usecase: CheckInUseCase

    @Inject
    lateinit var repository: ProfileRepository

    private var existProfile1: ProfileEntity =
        ProfileEntityImpl("uid-for-check-in-test1", "display name1")

    private var existProfile2: ProfileEntity =
        ProfileEntityImpl("uid-for-check-in-test2", "display name2")

    override fun onBefore() {
        super.onBefore()
        hiltRule.inject()
        runTest(timeout = Duration.INFINITE) {
            existProfile1 = with(repository) {
                setProfile(existProfile1)
                getProfile(existProfile1.uid)
            }

            existProfile2 = with(repository) {
                setProfile(existProfile2)
                getProfile(existProfile2.uid)
            }
        }
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        assertThrows(ProfileRepository.ProfileException.EmptyUserId::class.java) {
            runBlocking { usecase("", this) }
        }

        assertThrows(ProfileRepository.ProfileException.ProfileNotFound::class.java) {
            runBlocking { usecase("no uid", this) }
        }

        usecase(existProfile1, this)
        existProfile1 = repository.getProfile(existProfile1.uid)
        assertTrue(existProfile1 is ProfileEntity.OnlineUserEntity)

        coroutineScope {
            usecase(ProfileEntityImpl("", ""), this) {
                when (it) {
                    is UseCase.Result.Error -> when (it.e) {
                        CheckInUseCase.EmptyUserId -> {}
                        CheckInUseCase.UnKnownUser -> throw Exception("UnKnownUser")
                    }

                    is UseCase.Result.Exception -> throw it.t
                    is UseCase.Result.Success -> throw Exception("is not success")
                }
            }
        }

        coroutineScope {
            usecase(ProfileEntityImpl("no uid", ""), this) {
                when (it) {
                    is UseCase.Result.Error -> when (it.e) {
                        CheckInUseCase.EmptyUserId -> throw Exception("EmptyUserId")
                        CheckInUseCase.UnKnownUser -> {}
                    }

                    is UseCase.Result.Exception -> throw it.t
                    is UseCase.Result.Success -> throw Exception("is not success")
                }
            }
        }

        coroutineScope {
            usecase(existProfile2, this) {
                when (it) {
                    is UseCase.Result.Error -> when (it.e) {
                        CheckInUseCase.EmptyUserId -> throw Exception("EmptyUserId")
                        CheckInUseCase.UnKnownUser -> throw Exception("UnKnownUser")
                    }

                    is UseCase.Result.Exception -> throw it.t
                    is UseCase.Result.Success -> {}
                }
            }
        }
    }
}