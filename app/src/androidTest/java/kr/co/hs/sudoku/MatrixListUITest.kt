package kr.co.hs.sudoku

import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.HiltTestUtil.launchFragmentInHiltContainer
import kr.co.hs.sudoku.di.repositories.BeginnerMatrixRepositoryQualifier
import kr.co.hs.sudoku.feature.single.SingleDashboardFragment
import kr.co.hs.sudoku.model.matrix.BeginnerMatrix
import kr.co.hs.sudoku.repository.stage.MatrixRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.time.Duration

@Suppress("TestFunctionName", "NonAsciiCharacters")
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MatrixListUITest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    @BeginnerMatrixRepositoryQualifier
    lateinit var repository: MatrixRepository<BeginnerMatrix>

    @Before
    fun before() {
        hiltRule.inject()
    }

    @Test
    fun SinglePlay_선택_Fragment_테스트() =
        runTest(StandardTestDispatcher(), timeout = Duration.INFINITE) {
            val list = withContext(Dispatchers.IO) {
                repository.getList()
            }

            val fragmentScenario = launchFragmentInHiltContainer<SingleDashboardFragment>(
                themeResId = R.style.Theme_HSSudoku2,
                initialState = Lifecycle.State.RESUMED
            )

            fragmentScenario.onFragment {
                it.updateUIMatrixList(list)
            }

            runBlocking { delay(1000) }
        }

}