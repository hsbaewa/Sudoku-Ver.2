package kr.co.hs.sudoku.datasource.record.impl

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.record.RecordRemoteSource
import kr.co.hs.sudoku.model.record.ClearTimeRecordModel

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


    override suspend fun addRecord(challengeId: String, record: ClearTimeRecordModel): Boolean =
        getRankingCollection(challengeId)
            .document(record.uid).set(record, SetOptions.merge())
            .await()
            .run { true }

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
}