package kr.co.hs.sudoku.repository.timer

import android.os.SystemClock
import kr.co.hs.sudoku.core.Timer
import kr.co.hs.sudoku.datasource.timer.impl.TimeRemoteSourceImpl

class RealServerTimer : Timer {
    override fun start() = getCurrentTime().also { start = it }
    private var start = -1L

    fun getCurrentTime() = initTimeStamp + (SystemClock.elapsedRealtime() - (initElapsedTime))
    suspend fun initTime(): Boolean {
        val remoteSource = TimeRemoteSourceImpl()
        return remoteSource.getServerTimestamp()?.serverTime?.toDate()?.run {
            initElapsedTime = SystemClock.elapsedRealtime()
            initTimeStamp = time
            true
        } ?: false
    }

    private var initElapsedTime = 0L
    private var initTimeStamp = 0L

    override fun getStartTime() = start.takeIf { it >= 0 } ?: start()
    override fun getPassedTime() = getCurrentTime() - getStartTime() + passedTime
    override fun getTickInterval() = 50L
    override fun finish() = getPassedTime().also { finish = it }
    private var finish = -1L
    override fun getFinishTime() = finish.takeIf { it >= 0 } ?: finish()

    fun pass(millis: Long) {
        passedTime = millis
    }

    private var passedTime = 0L
}