package kr.co.hs.sudoku.viewmodel

import android.os.SystemClock
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.model.gamelog.impl.CellLogStreamEntityImpl
import kr.co.hs.sudoku.model.gamelog.impl.ValueChangedLogCollectEntity
import kr.co.hs.sudoku.model.stage.Stage

class TimerLogViewModel : TimerViewModel() {
    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/13
     * @comment 로그를 기록하면서 타이머 시작
     * @param to 로깅을 위한 stage
     **/
    fun startWithRecord(to: Stage): Job {
        valueChangedCollector.clear()
        to.addValueChangedListener(valueChangedCollector)
        this.stage = to
        this.stageTemplate = to.toValueTable()
        return super.start()
    }

    // 셀 값 변경 로그를 저장하는 큐
    private val valueChangedCollector = ValueChangedCollector()

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/13
     * @comment 상대적으로 흘러간 시간을 계산하기 위해 변경 불가한 System.elapsedRealtime()을 사용하기 위해 ValueChangedLogCollectEntity를 재정의
     **/
    private class ValueChangedCollector : ValueChangedLogCollectEntity() {
        fun startCollect(time: Long) = kotlin.run { startedElapsedRealTime = time }
        private var startedElapsedRealTime = -1L
        override fun getPassedTime() = startedElapsedRealTime.takeIf { it >= 0 }
            ?.run { SystemClock.elapsedRealtime() - this }
            ?: -1L
    }

    private var stage: Stage? = null

    // 처음 시작시 기본 값들
    var stageTemplate: List<List<Int>>? = null

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/13
     * @comment 그냥 시작 시에는 로그를 저장 안시킴
     **/
    override fun start(): Job {
        this.stage?.removeValueChangedListener(valueChangedCollector)
        this.stage = null
        return super.start()
    }

    override fun onStartTimer(from: Long) {
        super.onStartTimer(from)
        valueChangedCollector.startCollect(from)
    }

    override fun onRunningTimer(after: Long) {
        super.onRunningTimer(after)
        // 타이머를 그냥 시작한건지 로그를 저장하면서 시작한건지 구분하기 위해 stage null여부로 판단한다.
        if (stage == null) {
            viewModelScope.launch { valueLogStream.pop(after) }
        }
    }

    val valueLogStream = CellLogStreamEntityImpl(valueChangedCollector)
    fun remainStream() = valueChangedCollector.isNotEmpty()


    override fun onCleared() {
        super.onCleared()
        stage?.removeValueChangedListener(valueChangedCollector)
        stage = null
        valueChangedCollector.clear()
    }
}