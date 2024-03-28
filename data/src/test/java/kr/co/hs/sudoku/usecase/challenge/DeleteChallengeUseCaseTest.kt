package kr.co.hs.sudoku.usecase.challenge

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.FirebaseTest
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.usecase.NoErrorUseCase
import kr.co.hs.sudoku.usecase.UseCaseFlow
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
class DeleteChallengeUseCaseTest : FirebaseTest() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var usecase: DeleteChallengeUseCase

    @Inject
    lateinit var createUseCase: CreateChallengeUseCase

    lateinit var challenge: ChallengeEntity

    @Inject
    lateinit var getUseCase: GetChallengeUseCase

    override fun onBefore() = runTest(timeout = Duration.INFINITE) {
        super.onBefore()
        hiltRule.inject()

        createUseCase(9, 50.0)
            .collect { challenge = it.getSuccessData() ?: throw Exception("error") }
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        var getResult =
            getUseCase(challenge.challengeId).firstOrNull() as? UseCaseFlow.Result.Success
        var getData = getResult?.data
        assertNotNull(getData)

        val result = usecase(challenge).firstOrNull() as? NoErrorUseCase.Result.Success
        assertNotNull(result)

        getResult =
            getUseCase(challenge.challengeId).firstOrNull() as? UseCaseFlow.Result.Success
        getData = getResult?.data
        assertNull(getData)
    }
}