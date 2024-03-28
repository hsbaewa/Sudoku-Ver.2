package kr.co.hs.sudoku.usecase.challenge

import kr.co.hs.sudoku.core.Timer

class TestTimer: Timer {

    override fun start() = getCurrentTime().also { start = it }
    private var start = -1L
    private fun getCurrentTime() = System.currentTimeMillis()
    override fun getStartTime() = start.takeIf { it >= 0 } ?: start()
    override fun getPassedTime() = getCurrentTime() - getStartTime()
    override fun getTickInterval() = 50L
    override fun finish() = (getCurrentTime() - getStartTime()).also { finish = it }
    private var finish = -1L
    override fun getFinishTime() = finish.takeIf { it >= 0 } ?: finish()
}
