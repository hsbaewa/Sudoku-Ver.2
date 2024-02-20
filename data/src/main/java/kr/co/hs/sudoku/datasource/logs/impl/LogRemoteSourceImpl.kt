package kr.co.hs.sudoku.datasource.logs.impl

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.FireStoreRemoteSource
import kr.co.hs.sudoku.datasource.logs.LogRemoteSource
import kr.co.hs.sudoku.mapper.Mapper.asMutableMap
import kr.co.hs.sudoku.model.logs.LogModel
import kr.co.hs.sudoku.model.logs.impl.BattleClearModelImpl
import kr.co.hs.sudoku.model.logs.impl.ChallengeClearModelImpl
import java.util.Date

class LogRemoteSourceImpl : FireStoreRemoteSource(), LogRemoteSource {
    private val logsCollection: CollectionReference
        get() = rootDocument.collection("logs")

    override suspend fun getLogs(time: Date, count: Long): List<LogModel> = logsCollection
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .whereLessThan("createdAt", time)
        .limit(count)
        .get()
        .await()
        .mapNotNull { it.toLogData() }

    private fun DocumentSnapshot.toLogData() =
        when (runCatching { getString("type") }.getOrNull()) {
            "challengeClear" -> toObject(ChallengeClearModelImpl::class.java)
                ?.also { it.id = this.id }

            "battleClear" -> toObject(BattleClearModelImpl::class.java)
                ?.also { it.id = this.id }

            else -> null
        }

    override suspend fun getLogs(uid: String, time: Date, count: Long): List<LogModel> =
        logsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .whereLessThan("createdAt", time)
            .whereEqualTo("uid", uid)
            .limit(count)
            .get()
            .await()
            .mapNotNull { it.toLogData() }

    override suspend fun createLog(logModel: LogModel) {
        logsCollection
            .document()
            .set(
                when (logModel) {
                    is LogModel.BattleClear -> logModel
                        .asMutableMap()
                        .also {
                            it["type"] = "battleClear"
                            it["createdAt"] = FieldValue.serverTimestamp()
                        }

                    is LogModel.ChallengeClear -> logModel
                        .asMutableMap()
                        .also {
                            it["type"] = "challengeClear"
                            it["createdAt"] = FieldValue.serverTimestamp()
                        }
                }
            )
            .await()
    }

    override suspend fun deleteLog(logId: String) {
        logsCollection.document(logId).delete().await()
    }
}