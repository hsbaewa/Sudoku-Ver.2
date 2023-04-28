package kr.co.hs.sudoku.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.extension.NumberExtension.toTimerFormat
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.model.stage.history.HistoryItem
import kr.co.hs.sudoku.model.stage.history.HistoryQueue
import kr.co.hs.sudoku.repository.timer.Timer
import kr.co.hs.sudoku.usecase.timelog.TimerUseCase
import kr.co.hs.sudoku.usecase.timelog.TimerUseCaseImpl

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

    fun startTimer() = viewModelScope.launch {
        with(timerCore) {
            val timerUseCase: TimerUseCase = TimerUseCaseImpl(this)
            timerUseCase()
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
        _timer.value = stage.getCompletedTime().toTimerFormat()
    }

    private val _timer = MutableLiveData<String>()
    val timer: LiveData<String> by this::_timer

    private fun onRunning(time: Long) {
        _timer.value = time.toTimerFormat()
    }

    fun stopTimer() {
        timerJob?.cancel()
    }

    fun clearTime() {
        _timer.value = 0L.toTimerFormat()
    }


    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- Cell History ---------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    lateinit var historyQueue: HistoryQueue


    fun setHistoryWriter(writer: HistoryQueue) {
        this.historyQueue = writer
        stage.startCaptureHistory(timerCore, historyQueue)
    }

    fun startLog() = viewModelScope.launch {
        while (!historyQueue.isEmpty()) {
            delay(timerCore.getTickInterval())
            historyQueue.pop(timerCore.getPassedTime())?.forEach {
                cellEventHistoryFlow.emit(it)
                if (it is HistoryItem.Set && it.isCompleted) {
                    stopTimer()
                    _timer.value = it.time.toTimerFormat()
                }
            }
        }
    }.apply { historyJob = this }

    val cellEventHistoryFlow = MutableSharedFlow<HistoryItem>()

    private var historyJob: Job? = null
}