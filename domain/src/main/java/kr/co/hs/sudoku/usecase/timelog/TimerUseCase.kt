package kr.co.hs.sudoku.usecase.timelog

import kotlinx.coroutines.flow.Flow

interface TimerUseCase {
    operator fun invoke(): Flow<Long>
}