package kr.co.hs.sudoku.usecase.timelog

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.model.stage.history.HistoryItem
import kr.co.hs.sudoku.repository.timer.Timer

class HistoryUseCaseImpl(
    private val timer: Timer
) : HistoryUseCase {
    override fun invoke(history: List<HistoryItem>) = flow {
        val mutableHistoryList = history.toMutableList()
        while (true) {
            delay(timer.getTickInterval())
            val time = timer.getPassedTime()
            mutableHistoryList.filter { it.time <= time }
                .also { mutableHistoryList.removeAll(it) }
                .takeIf { it.isNotEmpty() }
                ?.run { emitAll(asFlow()) }
            if (mutableHistoryList.isEmpty()) {
                break
            }
        }
    }


}