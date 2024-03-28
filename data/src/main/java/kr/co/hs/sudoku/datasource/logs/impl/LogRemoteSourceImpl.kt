package kr.co.hs.sudoku.datasource.logs.impl

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.FireStoreRemoteSource
import kr.co.hs.sudoku.datasource.logs.LogRemoteSource
import kr.co.hs.sudoku.mapper.Mapper.asMutableMap
import kr.co.hs.sudoku.model.logs.LogModel
import kr.co.hs.sudoku.model.logs.impl.BattleClearModelImpl
import kr.co.hs.sudoku.model.logs.impl.ChallengeClearModelImpl
import java.util.Date
import javax.inject.Inject

class LogRemoteSourceImpl
@Inject constructor() : FireStoreRemoteSource(), LogRemoteSource {
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

    override suspend fun <T : LogModel> getLogs(
        type: Class<T>,
        uid: String,
        time: Date,
        count: Long
    ) = logsCollection
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .whereLessThan("createdAt", time)
        .whereEqualTo("uid", uid)
        .whereEqualTo(
            "type", when {
                type.isAssignableFrom(LogModel.ChallengeClear::class.java) -> "challengeClear"
                type.isAssignableFrom(LogModel.BattleClear::class.java) -> "battleClear"
                else -> ""
            }
        )
        .limit(count)
        .get()
        .await()
        .mapNotNull {
            @Suppress("UNCHECKED_CAST")
            it.toLogData() as T
        }

    override suspend fun createLog(logModel: LogModel): String {
        val reference = logsCollection.document()
        logModel.id = reference.id
        reference.set(logModel.toData()).await()
        return logModel.id
    }

    private fun LogModel.toData() = when (this) {
        is LogModel.BattleClear -> asMutableMap()
            .also {
                it["type"] = "battleClear"
                it["createdAt"] = FieldValue.serverTimestamp()
            }

        is LogModel.ChallengeClear -> asMutableMap()
            .also {
                it["type"] = "challengeClear"
                it["createdAt"] = FieldValue.serverTimestamp()
            }
    }

    override fun createLog(t: Transaction, logModel: LogModel) =
        t.set(logsCollection.document(), logModel.toData())

    override suspend fun deleteLog(logId: String) {
        logsCollection.document(logId).delete().await()
    }

    override suspend fun getLog(id: String): LogModel {
        val result = logsCollection.document(id).get().await()
        if (!result.exists())
            throw NullPointerException("document is empty")
        return result.toLogData() ?: throw NullPointerException("can not to log data")
    }
}