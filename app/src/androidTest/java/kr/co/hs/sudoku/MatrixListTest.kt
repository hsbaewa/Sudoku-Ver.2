package kr.co.hs.sudoku

import androidx.fragment.app.testing.FragmentScenario
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
import kr.co.hs.sudoku.feature.matrixlist.BeginnerMatrixListFragment
import kr.co.hs.sudoku.repository.IntermediateMatrixRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TestFunctionName", "NonAsciiCharacters")
@RunWith(AndroidJUnit4::class)
class MatrixListTest {

    private lateinit var fragmentScenario: FragmentScenario<BeginnerMatrixListFragment>

    @Before
    fun initFragmentScenario() = runTest {
        fragmentScenario = launchFragmentInContainer(
            themeResId = R.style.Theme_HSSudoku2,
            initialState = Lifecycle.State.INITIALIZED
        )
    }

    @Test
    fun UI_테스트() = runTest(StandardTestDispatcher(), dispatchTimeoutMs = 100000000) {
        val repository = IntermediateMatrixRepository()
        val list = withContext(Dispatchers.IO) {
            repository.getList()
        }

        fragmentScenario.moveToState(Lifecycle.State.STARTED)
        fragmentScenario.onFragment {
            it.setList(list)
        }

        runBlocking { delay(1000) }
    }
}