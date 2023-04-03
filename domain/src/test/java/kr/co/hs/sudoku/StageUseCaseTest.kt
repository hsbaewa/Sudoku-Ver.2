package kr.co.hs.sudoku

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.model.stage.impl.AutoPlayStageImpl
import kr.co.hs.sudoku.model.stage.impl.StageBuilderImpl
import kr.co.hs.sudoku.repository.stage.StageRepository
import kr.co.hs.sudoku.usecase.GetStageUseCaseImpl
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StageUseCaseTest {
    @Test
    fun testBeginnerRepository() = runTest {
        val stageRepository: StageRepository.BeginnerStageRepository = mockk()
        every { stageRepository.getStage(0) } answers {
            with(StageBuilderImpl()) {
                setBox(2, 2)
                autoGenerate(
                    listOf(
                        listOf(0, 1, 1, 0),
                        listOf(1, 0, 0, 1),
                        listOf(1, 0, 0, 1),
                        listOf(0, 1, 1, 0)
                    )
                )
                build()
            }
        }

        val usecase = GetStageUseCaseImpl(stageRepository)
        val stage = usecase(0).first()
        println(stage)
        assertEquals(false, stage.isCompleted())

        val autoPlayStage = AutoPlayStageImpl(stage, 0)
        autoPlayStage.play()
        println(stage)
        assertEquals(true, stage.isCompleted())
    }

    @Test
    fun testIntermediateRepository() = runTest {
        val stageRepository: StageRepository = mockk()
        every { stageRepository.getStage(0) } answers {
            with(StageBuilderImpl()) {
                setBox(3, 3)
                autoGenerate(
                    listOf(
                        listOf(0, 1, 1, 0, 1, 0, 1, 1, 0),
                        listOf(1, 1, 0, 1, 0, 1, 0, 1, 1),
                        listOf(0, 1, 0, 0, 1, 0, 0, 1, 0),
                        listOf(0, 1, 0, 1, 0, 1, 0, 1, 0),
                        listOf(1, 0, 1, 0, 1, 0, 1, 0, 1),
                        listOf(0, 1, 0, 1, 0, 1, 0, 1, 0),
                        listOf(0, 1, 0, 0, 1, 0, 0, 1, 0),
                        listOf(1, 1, 0, 1, 0, 1, 0, 1, 1),
                        listOf(0, 1, 1, 0, 1, 0, 1, 1, 0)
                    )
                )
                build()
            }
        }

        val usecase = GetStageUseCaseImpl(stageRepository)
        val stage = usecase(0).first()
        println(stage)
        assertEquals(false, stage.isCompleted())

        val autoPlayStage = AutoPlayStageImpl(stage, 0)
        autoPlayStage.play()
        println(stage)
        assertEquals(true, stage.isCompleted())
    }

    @Test
    fun testAdvancedRepository() = runTest {
        val stageRepository: StageRepository = mockk()
        every { stageRepository.getStage(0) } answers {
            with(StageBuilderImpl()) {
                setBox(4, 4)
                autoGenerate(
                    listOf(
                        listOf(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0),
                        listOf(0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0),
                        listOf(0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0),
                        listOf(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0),
                        listOf(0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0),
                        listOf(0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0),
                        listOf(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0),
                        listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                        listOf(0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0),
                        listOf(0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0),
                        listOf(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0),
                        listOf(0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0),
                        listOf(0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0),
                        listOf(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0),
                        listOf(0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0),
                        listOf(0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0)
                    )
                )
                build()
            }
        }

        val usecase = GetStageUseCaseImpl(stageRepository)
        val stage = usecase(0).first()
        println(stage)
        assertEquals(false, stage.isCompleted())

        val autoPlayStage = AutoPlayStageImpl(stage, 0)
        autoPlayStage.play()
        println(stage)
        assertEquals(true, stage.isCompleted())
    }

}