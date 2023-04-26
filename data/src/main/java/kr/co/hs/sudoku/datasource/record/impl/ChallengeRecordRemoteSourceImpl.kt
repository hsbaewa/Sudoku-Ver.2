package kr.co.hs.sudoku.datasource.record.impl

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.record.RecordRemoteSource
import kr.co.hs.sudoku.model.record.ClearTimeRecordModel
import kr.co.hs.sudoku.model.record.ReserveRecordModel
import kotlin.reflect.full.memberProperties

class ChallengeRecordRemoteSourceImpl : RecordRemoteSource {

    override suspend fun getRecords(challengeId: String, limit: Int) =
        getRankingCollection(challengeId)
            .orderBy("clearTime")
            .limit(limit.toLong())
            .get()
            .await()
            .documents.mapNotNull {
                it.toObject(ClearTimeRecordModel::class.java)
            }.onEachIndexed { index, model ->
                model.rank = index.toLong() + 1
            }

    private fun getRankingCollection(challengeId: String) = FirebaseFirestore.getInstance()
        .collection("version")
        .document("v2")
        .collection("challenge")
        .document(challengeId)
        .collection("record")


    override suspend fun setRecord(challengeId: String, record: ClearTimeRecordModel): Boolean =
        getRankingCollection(challengeId)
            .document(record.uid).set(record)
            .await()
            .run { true }

    override suspend fun setRecord(challengeId: String, record: ReserveRecordModel): Boolean {
        return getRankingCollection(challengeId)
            .document(record.uid)
            .set(
                record
                    .asMap()
                    .toMutableMap()
                    .apply { this["startAt"] = FieldValue.serverTimestamp() }
            )
            .await()
            .run { true }
    }

    private inline fun <reified T : Any> T.asMap(): Map<String, Any?> {
        val props = T::class.memberProperties.associateBy { it.name }
        return props.keys.associateWith { props[it]?.get(this) }
    }

    override suspend fun getRecord(challengeId: String, uid: String): ClearTimeRecordModel =
        getRankingCollection(challengeId)
            .document(uid)
            .get()
            .await()
            .toObject(ClearTimeRecordModel::class.java)
            ?.apply {
                rank = getRankingCollection(challengeId)
                    .orderBy("clearTime")
                    .whereLessThan("clearTime", clearTime)
                    .count()
                    .get(AggregateSource.SERVER)
                    .await()
                    .count + 1
            }
            ?: throw NullPointerException("cannot parse to ClearTimeRecordModel")

    override suspend fun getReservedMyRecord(challengeId: String, uid: String): ReserveRecordModel {
        return getRankingCollection(challengeId)
            .document(uid)
            .get()
            .await()
            .toObject(ReserveRecordModel::class.java)
            ?: throw Exception("not reserved record")
    }
}