package kr.co.hs.sudoku.feature.user

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.data.TestProfileDataSource
import kr.co.hs.sudoku.repository.user.TestProfileRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration

class AuthenticatorTest {

    private lateinit var authenticator: Authenticator

    @Before
    fun before() {
        val dataSource = TestProfileDataSource()
        authenticator = TestAuthenticator(dataSource, TestProfileRepository(dataSource))
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        assertThrows(Authenticator.RequireSignIn::class.java) {
            runBlocking { authenticator.getProfile().first() }
        }

        val signedProfile = authenticator.signIn().first()
        assertEquals(signedProfile, authenticator.getProfile().first())
    }
}