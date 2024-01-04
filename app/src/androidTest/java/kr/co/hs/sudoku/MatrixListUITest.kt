package kr.co.hs.sudoku

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.feature.single.SingleDashboardFragment
import kr.co.hs.sudoku.repository.BeginnerMatrixRepository
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration

@Suppress("TestFunctionName", "NonAsciiCharacters")
@RunWith(AndroidJUnit4::class)
class MatrixListUITest {

    @Test
    fun SinglePlay_선택_Fragment_테스트() =
        runTest(StandardTestDispatcher(), timeout = Duration.INFINITE) {
            val repository = BeginnerMatrixRepository()
            val list = withContext(Dispatchers.IO) {
                repository.getList()
            }

            val fragmentScenario = launchFragmentInContainer<SingleDashboardFragment>(
                themeResId = R.style.Theme_HSSudoku2,
                initialState = Lifecycle.State.RESUMED
            )

            fragmentScenario.onFragment {
                it.updateUIMatrixList(list)
            }

            runBlocking { delay(1000) }
        }

}