package kr.co.hs.sudoku.usecase.history

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.FirebaseTest
import kr.co.hs.sudoku.feature.user.Authenticator
import kr.co.hs.sudoku.usecase.UseCaseFlow
import kr.co.hs.sudoku.usecase.user.GetCurrentUserProfileUseCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date
import javax.inject.Inject
import kotlin.time.Duration

@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class GetHistoryListUseCaseTest : FirebaseTest() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var authenticator: Authenticator

    @Inject
    lateinit var getCurrentUserProfileUsecase: GetCurrentUserProfileUseCase

    @Inject
    lateinit var usecase: GetHistoryListUseCase

    override fun onBefore() = runTest(timeout = Duration.INFINITE) {
        super.onBefore()
        hiltRule.inject()
        authenticator.signIn().firstOrNull()
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        val result = usecase(
            Date(), 1
        ).mapNotNull {
            (it as? UseCaseFlow.Result.Success)?.data
        }.toList()

        assertTrue(result.isNotEmpty())
    }
}