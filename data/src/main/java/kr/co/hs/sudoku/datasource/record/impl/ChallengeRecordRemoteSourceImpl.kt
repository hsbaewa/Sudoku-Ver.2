package kr.co.hs.sudoku.datasource.record.impl

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.FireStoreRemoteSource
import kr.co.hs.sudoku.datasource.record.RecordRemoteSource
import kr.co.hs.sudoku.mapper.Mapper.asMutableMap
import kr.co.hs.sudoku.model.record.ClearTimeRecordModel
import kr.co.hs.sudoku.model.record.ReserveRecordModel

class ChallengeRecordRemoteSourceImpl : FireStoreRemoteSource(), RecordRemoteSource {

    override suspend fun getRecords(id: String, limit: Int) =
        getRankingCollection(id)
            .orderBy("clearTime")
            .limit(limit.toLong())
            .whereGreaterThan("clearTime", 0)
            .get()
            .await()
            .documents.mapNotNull {
                it.toObject(ClearTimeRecordModel::class.java)
            }.onEachIndexed { index, model ->
                model.rank = index.toLong() + 1
            }

    private fun getRankingCollection(challengeId: String) = rootDocument
        .collection("challenge")
        .document(challengeId)
        .collection("record")


    override suspend fun setRecord(id: String, record: ClearTimeRecordModel) =
        runCatching {
            getRankingCollection(id).document(record.uid).set(record).await()
            true
        }.getOrDefault(false)

    override suspend fun setRecord(id: String, record: ReserveRecordModel) =
        runCatching {
            getRankingCollection(id)
                .document(record.uid)
                .set(
                    record
                        .asMutableMap()
                        .apply { this["startAt"] = FieldValue.serverTimestamp() }
                )
                .await()
            true
        }.getOrDefault(false)

    override suspend fun getRecord(id: String, uid: String): ClearTimeRecordModel =
        getRankingCollection(id)
            .document(uid)
            .get()
            .await()
            .toObject(ClearTimeRecordModel::class.java)
            ?.apply {
                rank = getRankingCollection(id)
                    .orderBy("clearTime")
                    .whereLessThan("clearTime", clearTime)
                    .count()
                    .get(AggregateSource.SERVER)
                    .await()
                    .count + 1
            }
            ?: throw NullPointerException("cannot parse to ClearTimeRecordModel")

    override suspend fun getReservedMyRecord(id: String, uid: String): ReserveRecordModel {
        return getRankingCollection(id)
            .document(uid)
            .get()
            .await()
            .toObject(ReserveRecordModel::class.java)
            ?: throw Exception("not reserved record")
    }

    override suspend fun deleteRecord(id: String, uid: String): Boolean {
        return runCatching {
            getRankingCollection(id)
                .document(uid)
                .delete()
                .await()
            true
        }.getOrDefault(false)
    }
}