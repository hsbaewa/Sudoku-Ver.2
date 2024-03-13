package kr.co.hs.sudoku

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.datasource.impl.StageRemoteSourceImpl
import kr.co.hs.sudoku.model.sudoku.impl.CustomStageModelImpl
import kr.co.hs.sudoku.repository.*
import kr.co.hs.sudoku.usecase.SudokuBuildUseCase
import kr.co.hs.sudoku.usecase.SudokuGenerateUseCase
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import kotlin.time.Duration

class StageTest {

    lateinit var stageRemoteSource: StageRemoteSourceImpl

    lateinit var sudokuGenerator: SudokuGenerateUseCase
    lateinit var sudokuBuilder: SudokuBuildUseCase

    @Before
    fun initializeRemoteSource() = runTest {
        val beginnerSourceRes = javaClass.classLoader?.getResource("beginnerSource.json")
        val beginnerStream = ByteArrayOutputStream()
        withContext(Dispatchers.IO) {
            beginnerSourceRes?.openStream()
        }.use {
            it?.copyTo(beginnerStream)
        }
        val intermediateSourceRes = javaClass.classLoader?.getResource("intermediateSource.json")
        val intermediateStream = ByteArrayOutputStream()
        withContext(Dispatchers.IO) {
            intermediateSourceRes?.openStream()
        }.use {
            it?.copyTo(intermediateStream)
        }
        val advancedSourceRes = javaClass.classLoader?.getResource("advancedSource.json")
        val advancedStream = ByteArrayOutputStream()
        withContext(Dispatchers.IO) {
            advancedSourceRes?.openStream()
        }.use {
            it?.copyTo(advancedStream)
        }

        stageRemoteSource = StageRemoteSourceImpl(
            String(beginnerStream.toByteArray()),
            String(intermediateStream.toByteArray()),
            String(advancedStream.toByteArray())
        )

        mockkStatic(FirebaseRemoteConfig::class)
        every { FirebaseRemoteConfig.getInstance() } returns mockk(relaxed = true)

        sudokuGenerator = SudokuGenerateUseCase()
        sudokuBuilder = SudokuBuildUseCase()
    }

    @Test
    fun testStageRemoteSource() = runTest(timeout = Duration.INFINITE) {
        assertEquals(3, stageRemoteSource.getBeginnerGenerateMask().size)
        assertEquals(1, stageRemoteSource.getIntermediateGenerateMask().size)
        assertEquals(1, stageRemoteSource.getAdvancedGenerateMask().size)
    }

    @Test
    fun testBeginnerStageRepository() = runTest(timeout = Duration.INFINITE) {
        val matrixRepository = BeginnerMatrixRepository(stageRemoteSource)

        val matrix = matrixRepository.getList()[0]
        val stage = sudokuGenerator(matrix, this)
        println(stage)
        assertEquals(false, stage.isSudokuClear())
        assertEquals(0, stage.getDuplicatedCellCount())
        assertEquals(true, stage.getEmptyCellCount() > 0)

        SudokuPlayer(stage, 2000).flow.collect()

        println(stage)
        assertEquals(true, stage.isSudokuClear())
    }

    @Test
    fun testIntermediateStageRepository() = runTest(timeout = Duration.INFINITE) {
        val matrixRepository = IntermediateMatrixRepository(stageRemoteSource)

        val matrix = matrixRepository.getList()[0]
        val stage = sudokuGenerator(matrix, this)
        println(stage)
        assertEquals(false, stage.isSudokuClear())
        assertEquals(0, stage.getDuplicatedCellCount())
        assertEquals(true, stage.getEmptyCellCount() > 0)

        SudokuPlayer(stage, 0).flow.collect()

        println(stage)
        assertEquals(true, stage.isSudokuClear())
    }

    @Test
    fun testCustomStageRepository() = runTest(timeout = Duration.INFINITE) {
        val sourceRes = javaClass.classLoader?.getResource("customSource.json")
        val stream = ByteArrayOutputStream()
        withContext(Dispatchers.IO) {
            sourceRes?.openStream()
        }.use {
            it?.copyTo(stream)
        }
        val model = Gson().fromJson(String(stream.toByteArray()), CustomStageModelImpl::class.java)

        val stage = sudokuBuilder(model.matrix, this)

        println(stage)
        assertEquals(false, stage.isSudokuClear())
        assertEquals(0, stage.getDuplicatedCellCount())
        assertEquals(true, stage.getEmptyCellCount() > 0)

        SudokuPlayer(stage, 0).flow.collect()

        println(stage)
        assertEquals(true, stage.isSudokuClear())
    }
}