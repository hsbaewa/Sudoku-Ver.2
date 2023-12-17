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
import kr.co.hs.sudoku.feature.matrixlist.AdvancedMatrixListFragment
import kr.co.hs.sudoku.feature.matrixlist.BeginnerMatrixListFragment
import kr.co.hs.sudoku.feature.matrixlist.IntermediateMatrixListFragment
import kr.co.hs.sudoku.repository.AdvancedMatrixRepository
import kr.co.hs.sudoku.repository.BeginnerMatrixRepository
import kr.co.hs.sudoku.repository.IntermediateMatrixRepository
import org.junit.Test
import org.junit.runner.RunWith


@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TestFunctionName", "NonAsciiCharacters")
@RunWith(AndroidJUnit4::class)
class MatrixListTest {

    @Test
    fun Beginner_UI_테스트() = runTest(StandardTestDispatcher(), dispatchTimeoutMs = 100000000) {
        val repository = BeginnerMatrixRepository()
        val list = withContext(Dispatchers.IO) {
            repository.getList()
        }

        val fragmentScenario = launchFragmentInContainer<BeginnerMatrixListFragment>(
            themeResId = R.style.Theme_HSSudoku2,
            initialState = Lifecycle.State.INITIALIZED
        )
        fragmentScenario.moveToState(Lifecycle.State.STARTED)
        fragmentScenario.onFragment {
            it.setList(list)
        }

        runBlocking { delay(1000) }
    }

    @Test
    fun Intermediate_UI_테스트() = runTest(StandardTestDispatcher(), dispatchTimeoutMs = 100000000) {
        val repository = IntermediateMatrixRepository()
        val list = withContext(Dispatchers.IO) {
            repository.getList()
        }

        val fragmentScenario = launchFragmentInContainer<IntermediateMatrixListFragment>(
            themeResId = R.style.Theme_HSSudoku2,
            initialState = Lifecycle.State.INITIALIZED
        )
        fragmentScenario.moveToState(Lifecycle.State.STARTED)
        fragmentScenario.onFragment {
            it.setList(list)
        }

        runBlocking { delay(1000) }
    }

    @Test
    fun Advanced_UI_테스트() = runTest(StandardTestDispatcher(), dispatchTimeoutMs = 100000000) {
        val repository = AdvancedMatrixRepository()
        val list = withContext(Dispatchers.IO) {
            repository.getList()
        }

        val fragmentScenario = launchFragmentInContainer<AdvancedMatrixListFragment>(
            themeResId = R.style.Theme_HSSudoku2,
            initialState = Lifecycle.State.INITIALIZED
        )
        fragmentScenario.moveToState(Lifecycle.State.STARTED)
        fragmentScenario.onFragment {
            it.setList(list)
        }

        runBlocking { delay(1000) }
    }
}