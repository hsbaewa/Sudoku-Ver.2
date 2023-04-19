package kr.co.hs.sudoku.datasource.challenge

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.model.challenge.ChallengeModel
import kotlin.reflect.full.memberProperties

class ChallengeRemoteSourceImpl : ChallengeRemoteSource {

    override suspend fun getLatestChallenge() = challengeCollection
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(1)
        .get()
        .await()
        .documents
        .first()
        .toObject(ChallengeModel::class.java)
        ?: throw Exception("cannot parse to ChallengeModel class")

    private val challengeCollection = FirebaseFirestore.getInstance()
        .collection("version")
        .document("v2")
        .collection("challenge")

    override suspend fun getChallenge(id: String) = challengeCollection
        .document(id)
        .get()
        .await()
        .toObject(ChallengeModel::class.java)
        ?: throw Exception("cannot parse to ChallengeModel class")

    override suspend fun createChallenge(challengeModel: ChallengeModel) =
        with(
            challengeModel.id.takeIf { it != null }
                ?.run { challengeCollection.document(this) }
                ?: challengeCollection.document()
        ) {
            challengeModel.id = id
            challengeModel.asMap().toMutableMap()
            set(
                challengeModel
                    .also { it.id = id }
                    .asMap()
                    .toMutableMap()
                    .also { it["createdAt"] = FieldValue.serverTimestamp() }
            )

            true
        }

    private inline fun <reified T : Any> T.asMap(): Map<String, Any?> {
        val props = T::class.memberProperties.associateBy { it.name }
        return props.keys.associateWith { props[it]?.get(this) }
    }

    override suspend fun removeChallenge(id: String) =
        with(challengeCollection.document(id)) {
            delete().await()
            true
        }

    override suspend fun getChallengeIds() =
        challengeCollection.get().await().documents.map { it.id }
}