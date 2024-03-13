package kr.co.hs.sudoku.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.core.Stage
import kr.co.hs.sudoku.core.Timer
import kr.co.hs.sudoku.core.history.HistoryItem
import kr.co.hs.sudoku.core.history.HistoryQueue
import kr.co.hs.sudoku.extension.NumberExtension.toTimerFormat

class RecordViewModel : ViewModel() {
    fun bind(stage: Stage) {
        this.stage = stage
    }

    private lateinit var stage: Stage

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- 타이머 기능 ------------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    fun setTimer(timer: Timer) {
        this.timerCore = timer
        stage.setTimer(timer)
    }

    private lateinit var timerCore: Timer

    private fun runTimer() = viewModelScope.launch {
        with(timerCore) {
            flow {
                while (true) {
                    delay(getTickInterval())
                    emit(getPassedTime())
                }
            }
                .onStart { onStart() }
                .onCompletion { onFinish() }
                .collect { onRunning(it) }
        }
    }.apply { timerJob = this }

    private var timerJob: Job? = null

    private fun Timer.onStart() {
        start()
    }

    private fun Timer.onFinish() {
        finish()
        // 종료시 history상의 타이머와 일치 시키기 위해 아래 작업 수행
        stage.takeIf { it.isSudokuClear() }
            ?.run {
                _timer.value = getClearTime().toTimerFormat()
            }
    }

    private val _timer = MutableLiveData<String>()
    val timer: LiveData<String> by this::_timer

    private fun onRunning(time: Long) {
        _timer.value = time.toTimerFormat()
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- Cell History ---------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private var historyQueue: HistoryQueue? = null


    fun setHistoryWriter(writer: HistoryQueue) {
        this.historyQueue = writer
    }

    private fun enableCaptureHistory() {
        historyQueue?.run { stage.startCaptureHistory(this) }
    }

    private fun disableCaptureHistory() = takeIf { this::stage.isInitialized }
        ?.run { stage.stopCaptureHistory() }

    private fun runHistoryEvent() = viewModelScope.launch {
        historyQueue?.let { queue ->
            while (!queue.isEmpty()) {
                delay(timerCore.getTickInterval())
                queue.pop(timerCore.getPassedTime())?.forEach {
                    cellEventHistoryFlow.emit(it)
                    if (it is HistoryItem.Set && it.isCompleted) {
                        _timer.value = it.time.toTimerFormat()
                    }
                }
            }
        }
    }.apply { historyJob = this }

    val cellEventHistoryFlow = MutableSharedFlow<HistoryItem>()

    private var historyJob: Job? = null

    private fun stopLog() {
        historyJob?.cancel()
        historyJob = null
    }

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- 통합 --------------------------------------------\\
    //--------------------------------------------------------------------------------------------\\

    fun isRunningCapturedHistoryEvent() = historyJob?.isActive ?: false
    fun play() {
        enableCaptureHistory()
        runTimer()
    }

    fun stop() {
        cancelTimer()
        disableCaptureHistory()
    }

    fun playCapturedHistory() {
        runHistoryEvent()
        runTimer()
    }

    fun stopCapturedHistory() {
        cancelTimer()
    }

    fun isRunningTimer() = timerJob != null
}