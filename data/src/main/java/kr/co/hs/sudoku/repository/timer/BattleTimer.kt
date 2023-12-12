package kr.co.hs.sudoku.repository.timer

import android.os.SystemClock
import kr.co.hs.sudoku.datasource.timer.impl.TimeRemoteSourceImpl
import java.util.Date

class BattleTimer : Timer {
    override fun start() = startedTime

    fun getCurrentTime() = initTimeStamp + (SystemClock.elapsedRealtime() - (initElapsedTime))
    suspend fun initTime(startedAt: Date): Boolean {
        val remoteSource = TimeRemoteSourceImpl()
        return remoteSource.getServerTimestamp()?.serverTime?.toDate()?.run {
            initElapsedTime = SystemClock.elapsedRealtime()
            initTimeStamp = time
            startedTime = startedAt.time
            true
        } ?: false
    }

    private var initElapsedTime = 0L
    private var initTimeStamp = 0L
    private var startedTime = 0L

    override fun getStartTime() = startedTime.takeIf { it >= 0 } ?: start()
    override fun getPassedTime() = getCurrentTime() - getStartTime()
    override fun getTickInterval() = 50L
    override fun finish() = getPassedTime().also { finish = it }
    private var finish = -1L
    override fun getFinishTime() = finish.takeIf { it >= 0 } ?: finish()
}