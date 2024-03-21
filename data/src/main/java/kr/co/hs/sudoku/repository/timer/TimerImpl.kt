package kr.co.hs.sudoku.repository.timer

import android.os.SystemClock
import kr.co.hs.sudoku.core.Timer

class TimerImpl : Timer {
    override fun start() = getCurrentTime().also { start = it }
    private var start = -1L
    private fun getCurrentTime() = SystemClock.elapsedRealtime()
    override fun getStartTime() = start.takeIf { it >= 0 } ?: start()
    override fun getPassedTime() = getCurrentTime() - getStartTime()
    override fun getTickInterval() = 50L
    override fun finish() = (getCurrentTime() - getStartTime()).also { finish = it }
    private var finish = -1L
    override fun getFinishTime() = finish.takeIf { it >= 0 } ?: finish()
}