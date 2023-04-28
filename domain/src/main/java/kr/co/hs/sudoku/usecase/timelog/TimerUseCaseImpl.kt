package kr.co.hs.sudoku.usecase.timelog

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.repository.timer.Timer

class TimerUseCaseImpl(
    private val timer: Timer
) : TimerUseCase {

    override fun invoke() = flow {
        while (true) {
            delay(timer.getTickInterval())
            emit(timer.getPassedTime())
        }
    }
}