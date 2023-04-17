package kr.co.hs.sudoku

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.datasource.impl.StageRemoteSourceFromConfig
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalCoroutinesApi::class)
class StageFromConfigTest {

    private lateinit var remoteConfig: FirebaseRemoteConfig

    @Before
    fun initialize() = runTest {
        remoteConfig = mockk()
        every { remoteConfig.getString(StageRemoteSourceFromConfig.CONFIG_BEGINNER) } answers {
            val outputStream = ByteArrayOutputStream()
            javaClass.classLoader.getResource("beginnerSource.json").openStream().use {
                it.copyTo(outputStream)
            }
            String(outputStream.toByteArray())
        }

        every { remoteConfig.getString(StageRemoteSourceFromConfig.CONFIG_INTERMEDIATE) } answers {
            val outputStream = ByteArrayOutputStream()
            javaClass.classLoader.getResource("intermediateSource.json").openStream().use {
                it.copyTo(outputStream)
            }
            String(outputStream.toByteArray())
        }

        every { remoteConfig.getString(StageRemoteSourceFromConfig.CONFIG_ADVANCED) } answers {
            val outputStream = ByteArrayOutputStream()
            javaClass.classLoader.getResource("advancedSource.json").openStream().use {
                it.copyTo(outputStream)
            }
            String(outputStream.toByteArray())
        }
    }

    @Test
    fun getBeginnerFromRemoteConfig() = runTest {
        val remoteSource = StageRemoteSourceFromConfig(remoteConfig)
        remoteSource.getBeginnerGenerateMask()

        assertEquals(3, remoteSource.getBeginnerGenerateMask().size)
        assertEquals(1, remoteSource.getIntermediateGenerateMask().size)
        assertEquals(1, remoteSource.getAdvancedGenerateMask().size)
    }
}