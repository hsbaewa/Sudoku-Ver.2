package kr.co.hs.sudoku

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.datasource.impl.StageRemoteSourceImpl
import kr.co.hs.sudoku.model.stage.impl.AutoPlayStageImpl
import kr.co.hs.sudoku.model.sudoku.impl.CustomStageModelImpl
import kr.co.hs.sudoku.repository.BeginnerStageRepositoryImpl
import kr.co.hs.sudoku.repository.CustomStageRepositoryImpl
import kr.co.hs.sudoku.repository.IntermediateStageRepositoryImpl
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalCoroutinesApi::class)
class Test {

    lateinit var stageRemoteSource: StageRemoteSourceImpl

    @Before
    fun initializeRemoteSource() = runTest {
        val beginnerSourceRes = javaClass.classLoader.getResource("beginnerSource.json")
        val beginnerStream = ByteArrayOutputStream()
        withContext(Dispatchers.IO) {
            beginnerSourceRes.openStream()
        }.use {
            it.copyTo(beginnerStream)
        }
        val intermediateSourceRes = javaClass.classLoader.getResource("intermediateSource.json")
        val intermediateStream = ByteArrayOutputStream()
        withContext(Dispatchers.IO) {
            intermediateSourceRes.openStream()
        }.use {
            it.copyTo(intermediateStream)
        }
        val advancedSourceRes = javaClass.classLoader.getResource("advancedSource.json")
        val advancedStream = ByteArrayOutputStream()
        withContext(Dispatchers.IO) {
            advancedSourceRes.openStream()
        }.use {
            it.copyTo(advancedStream)
        }

        stageRemoteSource = StageRemoteSourceImpl(
            String(beginnerStream.toByteArray()),
            String(intermediateStream.toByteArray()),
            String(advancedStream.toByteArray())
        )

        mockkStatic(FirebaseRemoteConfig::class)
        every { FirebaseRemoteConfig.getInstance() } returns mockk(relaxed = true)
    }

    @Test
    fun testStageRemoteSource() = runTest {
        assertEquals(3, stageRemoteSource.getBeginnerGenerateMask().size)
        assertEquals(1, stageRemoteSource.getIntermediateGenerateMask().size)
        assertEquals(1, stageRemoteSource.getAdvancedGenerateMask().size)
    }

    @Test
    fun testBeginnerStageRepository() = runTest {
        val stageRepository = BeginnerStageRepositoryImpl()
        stageRepository.setRemoteSource(stageRemoteSource)
        stageRepository.doRequestStageList()
        val stage = stageRepository[0]

        println(stage)
        assertEquals(false, stage.isCompleted())
        assertEquals(0, stage.getDuplicatedCellCount())
        assertEquals(true, stage.getEmptyCellCount() > 0)

        val autoPlay = AutoPlayStageImpl(stage, 0)
        autoPlay.play()

        println(stage)
        assertEquals(true, stage.isCompleted())
    }

    @Test
    fun testIntermediateStageRepository() = runTest {
        val stageRepository = IntermediateStageRepositoryImpl()
        stageRepository.setRemoteSource(stageRemoteSource)
        stageRepository.doRequestStageList()
        val stage = stageRepository[0]

        println(stage)
        assertEquals(false, stage.isCompleted())
        assertEquals(0, stage.getDuplicatedCellCount())
        assertEquals(true, stage.getEmptyCellCount() > 0)

        val autoPlay = AutoPlayStageImpl(stage, 0)
        autoPlay.play()

        println(stage)
        assertEquals(true, stage.isCompleted())
    }

    @Test
    fun testCustomStageRepository() = runTest {
        val sourceRes = javaClass.classLoader.getResource("customSource.json")
        val stream = ByteArrayOutputStream()
        withContext(Dispatchers.IO) {
            sourceRes.openStream()
        }.use {
            it.copyTo(stream)
        }
        val model = Gson().fromJson(String(stream.toByteArray()), CustomStageModelImpl::class.java)

        val stageRepository = CustomStageRepositoryImpl()
        stageRepository.initialize(model)
        val stage = stageRepository.getStage()

        println(stage)
        assertEquals(false, stage.isCompleted())
        assertEquals(0, stage.getDuplicatedCellCount())
        assertEquals(true, stage.getEmptyCellCount() > 0)

        val autoPlay = AutoPlayStageImpl(stage, 0)
        autoPlay.play()

        println(stage)
        assertEquals(true, stage.isCompleted())
    }
}