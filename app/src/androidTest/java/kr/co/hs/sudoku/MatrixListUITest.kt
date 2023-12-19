package kr.co.hs.sudoku

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.feature.single.SinglePlayListFragment
import kr.co.hs.sudoku.repository.BeginnerMatrixRepository
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TestFunctionName", "NonAsciiCharacters")
@RunWith(AndroidJUnit4::class)
class MatrixListUITest {

    @Test
    fun SinglePlay_선택_Fragment_테스트() =
        runTest(StandardTestDispatcher(), dispatchTimeoutMs = 10000000) {
            val repository = BeginnerMatrixRepository()
            val list = withContext(Dispatchers.IO) {
                repository.getList()
            }

            val fragmentScenario = launchFragmentInContainer<SinglePlayListFragment>(
                themeResId = R.style.Theme_HSSudoku2,
                initialState = Lifecycle.State.RESUMED
            )

            fragmentScenario.onFragment {
                it.updateUIMatrixList(list)
            }

            runBlocking { delay(1000) }
        }

}