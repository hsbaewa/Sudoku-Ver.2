package kr.co.hs.sudoku.datasource.rank.impl

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.rank.RankingRemoteSource
import kr.co.hs.sudoku.model.record.ClearTimeRecordModel

class ChallengeRankingRemoteSourceImpl(challengeId: String) : RankingRemoteSource {

    override suspend fun getRecords() = rankingCollection
        .orderBy("clearTime")
        .limit(10)
        .get()
        .await()
        .documents.mapNotNull {
            it.toObject(ClearTimeRecordModel::class.java)
        }.onEachIndexed { index, model ->
            model.rank = index.toLong() + 1
        }

    private val rankingCollection = FirebaseFirestore.getInstance()
        .collection("version")
        .document("v2")
        .collection("challenge")
        .document(challengeId)
        .collection("ranking")


    override suspend fun addRecord(record: ClearTimeRecordModel) =
        rankingCollection.document(record.uid).set(record, SetOptions.merge()).await().run { true }

    override suspend fun getRecord(uid: String) =
        rankingCollection.document(uid).get().await().toObject(ClearTimeRecordModel::class.java)
            ?.apply {
                rank = rankingCollection
                    .orderBy("clearTime")
                    .whereLessThan("clearTime", clearTime)
                    .count()
                    .get(AggregateSource.SERVER)
                    .await()
                    .count + 1
            }
            ?: throw NullPointerException("cannot parse to ClearTimeRecordModel")
}