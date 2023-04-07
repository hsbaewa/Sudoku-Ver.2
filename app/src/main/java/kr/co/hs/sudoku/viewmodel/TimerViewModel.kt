package kr.co.hs.sudoku.viewmodel

import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class TimerViewModel : ViewModel() {
    private val timerFlow = flow {
        while (true) {
            delay(50)
            emit(SystemClock.elapsedRealtime() - from)
        }
    }.onStart { from = SystemClock.elapsedRealtime() }
    private var from: Long = 0

    fun start() = viewModelScope.launch {
        timerFlow
            .onStart { 0L.toTimerFormat().run { _time.value = this } }
            .map { it.toTimerFormat() }
            .collect { _time.value = it }
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
    val time: LiveData<String> by this::_time

    private lateinit var timerJob: Job

    fun stop() = takeIf { isRunning() }?.run {
        this@TimerViewModel.timerJob.cancel()
        (SystemClock.elapsedRealtime() - from)
            .toTimerFormat()
            .run { _time.value = this }
    }

    fun isRunning() = this@TimerViewModel::timerJob.isInitialized && timerJob.isActive
}