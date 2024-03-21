package kr.co.hs.sudoku.usecase.user

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.FirebaseTest
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl
import kr.co.hs.sudoku.usecase.UseCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration

@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class CreateProfileUseCaseTest : FirebaseTest() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var usecase: CreateProfileUseCase

    override fun onBefore() {
        super.onBefore()
        hiltRule.inject()
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        val uid = "uid-for-create-${Random.nextInt()}"
        assertEquals(
            uid,
            usecase(ProfileEntityImpl(uid, "display name for create"), this).uid
        )

        usecase(ProfileEntityImpl(uid, "display name for create"), this) {
            when (it) {
                is UseCase.Result.Error -> when (it.e) {
                    CreateProfileUseCase.AlreadyUser -> {}
                    CreateProfileUseCase.EmptyUserId -> throw Exception("EmptyUserId")
                }

                is UseCase.Result.Exception -> throw it.t
                is UseCase.Result.Success -> throw Exception("is not success")
            }
        }


    }
}