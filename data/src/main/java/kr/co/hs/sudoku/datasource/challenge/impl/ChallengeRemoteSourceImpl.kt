package kr.co.hs.sudoku.datasource.challenge.impl

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.FireStoreRemoteSource
import kr.co.hs.sudoku.datasource.challenge.ChallengeRemoteSource
import kr.co.hs.sudoku.mapper.Mapper.asMutableMap
import kr.co.hs.sudoku.model.challenge.ChallengeModel
import java.util.Date
import javax.inject.Inject

class ChallengeRemoteSourceImpl
@Inject constructor() : FireStoreRemoteSource(), ChallengeRemoteSource {

    override suspend fun getLatestChallenge() = challengeCollection
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(1)
        .get()
        .await()
        .documents
        .first()
        .toObject(ChallengeModel::class.java)
        ?: throw Exception("cannot parse to ChallengeModel class")

    private val challengeCollection: CollectionReference
        get() = rootDocument.collection("challenge")

    override suspend fun getChallenge(id: String) = with(challengeCollection) {
        val document = document(id).get().await()
        if (!document.exists())
            throw NullPointerException("document is null")
        document.toObject(ChallengeModel::class.java)
            ?: throw Exception("cannot parse to ChallengeModel class")
    }

    override suspend fun createChallenge(challengeModel: ChallengeModel): String {
        return with(
            challengeModel.id.takeIf { !it.isNullOrEmpty() }
                ?.run { challengeCollection.document(this) }
                ?: challengeCollection.document()
        ) {
            challengeModel.id = id
            set(
                challengeModel
                    .also { it.id = id }
                    .asMutableMap()
                    .also { it["createdAt"] = FieldValue.serverTimestamp() }
            ).await()

            id
        }
    }

    override suspend fun createChallenge(
        challengeModel: ChallengeModel,
        createdAt: Date
    ): String {
        return with(
            challengeModel.id.takeIf { it != null }
                ?.run { challengeCollection.document(this) }
                ?: challengeCollection.document()
        ) {
            challengeModel.id = id
            set(
                challengeModel
                    .also { it.id = id }
                    .asMutableMap()
                    .also { it["createdAt"] = createdAt }
            ).await()

            id
        }
    }

    override suspend fun removeChallenge(id: String) =
        with(challengeCollection.document(id)) {
            delete().await()
            true
        }

    override suspend fun getChallengeIds() =
        challengeCollection.get().await().documents.map { it.id }

    override suspend fun getChallenge(createdAt: Date) =
        challengeCollection.whereEqualTo("createdAt", createdAt)
            .get()
            .await()
            .documents
            .first()
            .toObject(ChallengeModel::class.java)
            ?: throw Exception("not found")

    override suspend fun getChallenges(startAt: Date) = challengeCollection
        .whereGreaterThan("createdAt", startAt)
        .get()
        .await()
        .documents
        .mapNotNull { it.toObject(ChallengeModel::class.java) }

    override suspend fun getChallenges(count: Long) = challengeCollection
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(count)
        .get()
        .await()
        .documents
        .mapNotNull { it.toObject(ChallengeModel::class.java) }

    override suspend fun getChallenges(startAt: Date, count: Long) = challengeCollection
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .whereLessThan("createdAt", startAt)
        .limit(count)
        .get()
        .await()
        .documents
        .mapNotNull { it.toObject(ChallengeModel::class.java) }
}