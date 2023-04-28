package kr.co.hs.sudoku.datasource.timer

import kr.co.hs.sudoku.model.timer.ServerTime

interface TimeRemoteSource {
    suspend fun getServerTimestamp(): ServerTime?
}