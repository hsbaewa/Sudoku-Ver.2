package kr.co.hs.sudoku.datasource.challenge.impl

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.FireStoreRemoteSource
import kr.co.hs.sudoku.datasource.challenge.ChallengeRecordRemoteSource
import kr.co.hs.sudoku.datasource.logs.LogRemoteSource
import kr.co.hs.sudoku.mapper.Mapper.asMutableMap
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.challenge.ClearTimeRecordModel
import kr.co.hs.sudoku.model.challenge.ReserveRecordModel
import kr.co.hs.sudoku.model.logs.impl.ChallengeClearModelImpl
import javax.inject.Inject

class ChallengeRecordRemoteSourceImpl
@Inject constructor(
    private val logRemoteSource: LogRemoteSource
) : FireStoreRemoteSource(), ChallengeRecordRemoteSource {

    private fun collection(challengeId: String) = rootDocument
        .collection("challenge")
        .document(challengeId)
        .collection("record")

    private fun document(challengeId: String, uid: String) =
        collection(challengeId).document(uid)

    override suspend fun getRecords(id: String, limit: Int): List<ClearTimeRecordModel> =
        collection(id)
            .orderBy("clearTime")
            .limit(limit.toLong())
            .whereGreaterThan("clearTime", 0)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(ClearTimeRecordModel::class.java) }
            .onEachIndexed { index, model -> model.rank = index.toLong() + 1 }

    override suspend fun setRecord(id: String, record: ClearTimeRecordModel): Boolean =
        runCatching {
            FirebaseFirestore.getInstance().runTransaction {
                it.set(document(id, record.uid), record)

                logRemoteSource.createLog(
                    it,
                    ChallengeClearModelImpl()
                        .also {
                            it.uid = record.uid
                            it.challengeId = id
                            it.record = record.clearTime
                        }
                )
                true
            }.await()
        }.getOrDefault(false)

    override suspend fun setRecord(id: String, record: ReserveRecordModel): Boolean =
        runCatching {
            document(id, record.uid)
                .set(
                    record
                        .asMutableMap()
                        .apply { this["startAt"] = FieldValue.serverTimestamp() }
                )
                .await()
            true
        }.getOrDefault(false)

    override suspend fun getRecord(id: String, uid: String): ClearTimeRecordModel =
        document(id, uid)
            .get()
            .await()
            .toObject(ClearTimeRecordModel::class.java)
            ?.apply {
                rank = if (clearTime > 0) {
                    collection(id)
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

    override suspend fun getReservedMyRecord(id: String, uid: String): ReserveRecordModel =
        document(id, uid)
            .get()
            .await()
            .toObject(ReserveRecordModel::class.java)
            ?: throw Exception("not reserved record")

    override suspend fun deleteRecord(id: String, uid: String): Boolean =
        runCatching {
            document(id, uid).delete().await()
            true
        }.getOrDefault(false)

    override suspend fun getChallengeMetadata(
        challengeEntity: ChallengeEntity,
        uid: String
    ): Boolean = runCatching {
        val documentSnapshot = document(challengeEntity.challengeId, uid)
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