package kr.co.hs.sudoku.datasource.record.impl

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.FireStoreRemoteSource
import kr.co.hs.sudoku.datasource.logs.LogRemoteSource
import kr.co.hs.sudoku.datasource.logs.impl.LogRemoteSourceImpl
import kr.co.hs.sudoku.datasource.record.RecordRemoteSource
import kr.co.hs.sudoku.mapper.Mapper.asMutableMap
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.logs.impl.ChallengeClearModelImpl
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
            .takeIf { it }
            ?.apply {
                val logRemoteSource: LogRemoteSource =
                    LogRemoteSourceImpl().also { it.rootDocument = rootDocument }

                logRemoteSource
                    .runCatching {
                        val data = ChallengeClearModelImpl()
                            .also {
                                it.uid = record.uid
                                it.challengeId = id
                                it.record = record.clearTime
                            }
                        createLog(data)
                    }
                    .getOrNull()
            }
            ?: false

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
                rank = if (clearTime > 0) {
                    getRankingCollection(id)
                        .orderBy("clearTime")
                        .whereLessThan("clearTime", clearTime)
                        .count()
                        .get(AggregateSource.SERVER)
                        .await()
                        .count + 1
                } else {
                    throw NullPointerException("cannot parse to ClearTimeRecordModel")
                }
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

    override suspend fun getChallengeMetadata(
        challengeEntity: ChallengeEntity,
        uid: String
    ) = runCatching {
        val documentSnapshot = getRankingCollection(challengeEntity.challengeId)
            .document(uid)
            .get()
            .await()

        challengeEntity.startPlayAt = documentSnapshot.getTimestamp("startAt")?.toDate()
        challengeEntity.isPlaying = challengeEntity.startPlayAt != null
        challengeEntity.relatedUid = uid
        challengeEntity.isComplete = (documentSnapshot.getLong("clearTime") ?: -1) >= 0
        true
    }.getOrElse {
        challengeEntity.startPlayAt = null
        challengeEntity.isPlaying = false
        challengeEntity.relatedUid = uid
        challengeEntity.isComplete = false
        false
    }
}