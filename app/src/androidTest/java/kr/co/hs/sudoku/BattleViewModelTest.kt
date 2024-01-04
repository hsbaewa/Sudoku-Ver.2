package kr.co.hs.sudoku

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.battle.ParticipantEntity
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.feature.multi.play.MultiPlayViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.time.Duration

@Suppress("NonAsciiCharacters", "TestFunctionName")
@RunWith(AndroidJUnit4::class)
class BattleViewModelTest : BattleRepositoryTest() {

    private val viewModel = ArrayList<MultiPlayViewModel>()

    val testStartingMatrix = listOf(
        listOf(1, 0, 0, 1),
        listOf(0, 1, 1, 0),
        listOf(1, 0, 0, 1),
        listOf(0, 1, 1, 0)
    )
    lateinit var testBattleId: String

    override fun initRepository() = runTest {
        super.initRepository()
        initViewModel()

        userBattleRepository[3].create(CustomMatrix(testStartingMatrix))
        testBattleId = userBattleRepository[3].getParticipating().id
    }


    private fun initViewModel() {
        userProfile.indices.forEach {
            viewModel.add(MultiPlayViewModel(userBattleRepository[it]))
        }
    }

    override fun releaseRepository() = runTest {
        super.releaseRepository()
        userBattleRepository[3].runCatching { exit() }.getOrNull()

        releaseViewModels()
    }

    private fun releaseViewModels() = runTest {
        viewModel.forEach {
            it.runCatching { doExit() }.getOrNull()
        }
    }

    @Test
    override fun 게임_참여_테스트() = runTest(timeout = Duration.INFINITE) {
        viewModel[0].doJoin(testBattleId)

        var battleEntity = viewModel[0].battleEntity.getOrAwaitValue(10) {
            it.participants.size == 2 && it.participants.find { it.uid == userProfile[0].uid } != null
        }
        assertNotNull(battleEntity)

        assertThrows(Exception::class.java) {
            runBlocking { viewModel[1].doJoin(testBattleId) }
        }.also { assertEquals(it.message, "게임(${battleEntity.id})의 참여자가 2/2로 이미 가득 찼습니다.") }

        viewModel[3].startEventMonitoring(testBattleId)
        battleEntity = viewModel[3].battleEntity.getOrAwaitValue(10) {
            it.participants.size == 2
                    && it.participants.find { it.uid == userProfile[0].uid } != null
                    && it.host == userProfile[3].uid
        }
        assertNotNull(battleEntity)
    }

    @Test
    override fun 게임_시작_테스트() = runTest(timeout = Duration.INFINITE) {
        viewModel[0].doJoin(testBattleId)

        assertThrows(Exception::class.java) {
            runBlocking { viewModel[0].doStart() }
        }.also { assertEquals(it.message, "방이 게임을 시작 할 수 있는 상태가 아닙니다. 먼저 pending을 호출하세요.") }

        assertThrows(Exception::class.java) {
            runBlocking { viewModel[0].doPending() }
        }.also { assertEquals(it.message, "오직 방장(${userProfile[3].uid})만이 게임을 시작 할 수 있습니다.") }

        assertThrows(Exception::class.java) {
            runBlocking { viewModel[3].doPending() }
        }.also { assertEquals(it.message, "아직 모든 참여자가 준비가 되어 있지 않습니다.") }

        viewModel[0].doReady()

        viewModel[3].doPending()
        viewModel[3].doStart()

        val battleEntity = viewModel[0].battleEntity.getOrAwaitValue(10) {
            it is BattleEntity.Playing
        }
        assertNotNull(battleEntity)
        assertTrue(battleEntity.participants.size == 2 && battleEntity.participants.filter { it !is ParticipantEntity.Playing }
            .isEmpty())
    }

    @Test
    override fun 게임_종료_테스트() = runTest(timeout = Duration.INFINITE) {
        viewModel[0].doJoin(testBattleId)
        viewModel[0].doReady()

        viewModel[3].doPending()
        viewModel[3].doStart()
        viewModel[3].startEventMonitoring(testBattleId)

        var battleEntity = viewModel[3].battleEntity
            .getOrAwaitValue(10) { it is BattleEntity.Playing }

        assertEquals(battleEntity.host, userProfile[3].uid)

        viewModel[3].doExit()
        battleEntity = viewModel[0].battleEntity
            .getOrAwaitValue(10) { it.host == userProfile[0].uid }

        assertNotNull(battleEntity)

        viewModel[0].doExit()
        battleEntity = viewModel[3].battleEntity
            .getOrAwaitValue(10) { it is BattleEntity.Invalid }

        assertNotNull(battleEntity)
    }


    @Test
    override fun 게임_클리어_테스트() = runTest(timeout = Duration.INFINITE) {
        viewModel[0].doJoin(testBattleId)

        assertThrows(Exception::class.java) {
            runBlocking { viewModel[0].doClear(1000) }
        }.also {
            assertEquals(
                it.message,
                "아직 게임($testBattleId)이 시작 되지 않았습니다. 게임이 시작 된 이후에 기록을 저장 할 수 있습니다."
            )
        }

        viewModel[0].doReady()

        viewModel[3].doPending()
        viewModel[3].doStart()
        viewModel[3].startEventMonitoring(testBattleId)

        viewModel[0].doClear(1000)

        val battleEntity = viewModel[3].battleEntity
            .getOrAwaitValue(10) { it is BattleEntity.Closed && it.participants.find { it is ParticipantEntity.Cleared } != null }

        assertTrue(battleEntity.participants.find { it.uid == userProfile[0].uid } is ParticipantEntity.Cleared)
    }


    @Test
    override fun 셀_변경_테스트() = runTest(timeout = Duration.INFINITE) {
        viewModel[0].doJoin(testBattleId)

        assertThrows(Exception::class.java) {
            runBlocking { viewModel[0].doUpdateMatrix(0, 0, 2) }
        }.also { assertEquals(it.message, "게임이 아직 시작 되지 않았습니다.") }

        viewModel[0].doReady()

        viewModel[3].doPending()
        viewModel[3].doStart()
        viewModel[3].startEventMonitoring(testBattleId)

        assertThrows(Exception::class.java) {
            runBlocking { viewModel[0].doUpdateMatrix(0, 0, 2) }
        }.also { assertEquals(it.message, "변경이 불가능한 셀입니다.") }

        viewModel[0].doUpdateMatrix(0, 1, 2)

        val battleEntity = viewModel[3].battleEntity
            .getOrAwaitValue(10) { it is BattleEntity.Playing && it.participants.find { it is ParticipantEntity.Playing && it.matrix[0][1] == 2 } != null }

        assertNotNull(battleEntity)
    }


    /* Copyright 2019 Google LLC.
   SPDX-License-Identifier: Apache-2.0 */
    suspend inline fun <reified T> LiveData<T>.getOrAwaitValue(
        time: Long = 2,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        crossinline condition: (T) -> Boolean
    ): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {

            override fun onChanged(value: T) {
                if (condition(value)) {
                    data = value
                    latch.countDown()
                    this@getOrAwaitValue.removeObserver(this)
                }
            }
        }

        withContext(Dispatchers.Main) {
            this@getOrAwaitValue.observeForever(observer)
        }

        // Don't wait indefinitely if the LiveData is not set.
        if (!withContext(Dispatchers.IO) { latch.await(time, timeUnit) }) {
            throw TimeoutException("LiveData value was never set.")
        }

        return data as T
    }


}