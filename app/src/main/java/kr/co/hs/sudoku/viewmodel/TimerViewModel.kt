package kr.co.hs.sudoku.viewmodel

import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {
    private var from: Long = 0
    private val _time = MutableLiveData<String>()
    private val time: LiveData<String> by this::_time

    private val

    fun start() = viewModelScope.launch {
        from = SystemClock.elapsedRealtime()
        flow {
            while (isActive) {
                delay(200)
                val n = SystemClock.elapsedRealtime() - from
                emit(n)
            }
        }
    }
}