package kr.co.hs.sudoku.datasource.logs

import com.google.firebase.firestore.Transaction
import kr.co.hs.sudoku.model.logs.LogModel
import java.util.Date

interface LogRemoteSource {
    suspend fun getLogs(time: Date, count: Long): List<LogModel>
    suspend fun getLogs(uid: String, time: Date, count: Long): List<LogModel>
    suspend fun <T : LogModel> getLogs(
        type: Class<T>,
        uid: String,
        time: Date,
        count: Long
    ): List<T>

    suspend fun createLog(logModel: LogModel)
    fun createLog(t: Transaction, logModel: LogModel): Transaction
    suspend fun deleteLog(logId: String)
}