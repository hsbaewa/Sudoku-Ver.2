package kr.co.hs.sudoku.usecase.timelog

import kotlinx.coroutines.flow.Flow
import kr.co.hs.sudoku.model.stage.history.HistoryItem

interface HistoryUseCase {
    operator fun invoke(history: List<HistoryItem>): Flow<HistoryItem>
}