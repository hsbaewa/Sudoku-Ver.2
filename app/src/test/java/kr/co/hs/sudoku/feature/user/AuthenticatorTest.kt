package kr.co.hs.sudoku.feature.user

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.di.user.UserModule
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.time.Duration

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class AuthenticatorTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun before() {
        FirebaseApp.initializeApp(
            RuntimeEnvironment.getApplication(),
            FirebaseOptions.Builder()
                .setProjectId("")
                .setApiKey("")
                .setApplicationId("")
                .setGcmSenderId("")
                .setStorageBucket("")
                .build()
        )

        hiltRule.inject()
    }

    @After
    fun after() {
        FirebaseApp.clearInstancesForTest()
    }

    @Inject
    @UserModule.GoogleGamesAuthenticatorQualifier
    lateinit var authenticator: Authenticator

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        val profile = authenticator.signIn()
            .catch { throw it }
            .first()

        assertNotNull(profile)
    }
}