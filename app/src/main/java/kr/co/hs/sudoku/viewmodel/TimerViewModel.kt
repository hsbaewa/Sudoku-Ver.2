package kr.co.hs.sudoku.viewmodel

import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

open class TimerViewModel : ViewModel() {
    private val timerFlow = flow {
        while (true) {
            delay(getTickIntervalMillis())
            emit(getPassedTime())
        }
    }.onStart {
        from = SystemClock.elapsedRealtime()
            .also { onStartTimer(it) }
    }.onEach {
        onRunningTimer(it)
    }.onCompletion {
        if (it is StopTimerException) {
            onStopTimer(it.finishTime)
        }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/13
     * @comment 타이머 flow에서 이벤트를 방출하는 주기
     * @return 주기 Long type의 밀리 세컨
     **/
    private fun getTickIntervalMillis() = 50L

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/13
     * @comment 타이머 시작 시점 부터 흘러간 시간 출력
     * @return 흘러간 시간 long type
     **/
    private fun getPassedTime() = SystemClock.elapsedRealtime() - from

    // 시작 시간
    private var from: Long = 0

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/13
     * @comment 타이머가 시작 되는 시점에 호출됨
     * @param from 시작 시간
     **/
    open fun onStartTimer(from: Long) {}

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/13
     * @comment 타이머가 진행중일때 특정 interval 로 호출 되는 함수
     * @param after 타이머 시작 시점 부터 얼마나 흘렀는가?
     **/
    open fun onRunningTimer(after: Long) {}

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/13
     * @comment 타이머가 종료 되었을때 호출 되는 함수
     * @param finishTime from 부터 종료 시점 까지 흘러간 시간
     **/
    open fun onStopTimer(finishTime: Long) {}

    open fun start() = viewModelScope.launch {
        timerFlow
            .onStart { 0L.toTimerFormat().run { _time.value = this } }
            .map { it.toTimerFormat() }
            .collect {
                timerJob
                    .takeIf { job -> job.isActive }
                    ?.run { _time.value = it }
            }
    }.apply {
        stop()
        timerJob = this
    }

    private fun Long.toTimerFormat() = this.let { allMillis ->
        val millis = allMillis % 1000
        val allSeconds = allMillis / 1000
        val seconds = allSeconds % 60
        val allMinutes = allSeconds / 60
        val minutes = allMinutes % 60
        val hour = allMinutes / 60
        String.format("%d:%02d:%02d.%03d", hour, minutes, seconds, millis)
    }

    private val _time = MutableLiveData<String>()
    val time: LiveData<String> by ::_time

    private lateinit var timerJob: Job

    open fun stop(stopTime: Long? = null) = takeIf { isRunning() }?.run {
        val clearTime = stopTime.takeIf { it != null && it > 0 }
            ?.run { this }
            ?: kotlin.run { SystemClock.elapsedRealtime() - from }
        this@TimerViewModel.timerJob.cancel(StopTimerException(clearTime))
        clearTime
            .toTimerFormat()
            .run { _time.value = this }
    }

    private class StopTimerException(val finishTime: Long) : CancellationException()

    fun isRunning() = this@TimerViewModel::timerJob.isInitialized && timerJob.isActive
}