package kr.co.hs.sudoku.datasource.logs

import kr.co.hs.sudoku.model.logs.LogModel
import java.util.Date

interface LogRemoteSource {
    suspend fun getLogs(time: Date, count: Long): List<LogModel>
    suspend fun getLogs(uid: String, time: Date, count: Long): List<LogModel>
    suspend fun createLog(logModel: LogModel)
    suspend fun deleteLog(logId: String)
}