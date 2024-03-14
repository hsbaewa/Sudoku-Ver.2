package kr.co.hs.sudoku.datasource.user.impl

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.FireStoreRemoteSource
import kr.co.hs.sudoku.datasource.user.ProfileRemoteSource
import kr.co.hs.sudoku.model.user.ProfileModelImpl
import javax.inject.Inject

class ProfileRemoteSourceImpl
@Inject constructor() : FireStoreRemoteSource(), ProfileRemoteSource {

    private val profileCollection: CollectionReference
        get() = rootDocument.collection("profile")

    override suspend fun getProfile(uid: String) = profileCollection
        .document(uid)
        .get()
        .await()
        .toObject(ProfileModelImpl::class.java)
        ?: throw NullPointerException("document is null")

    override fun getProfile(transaction: Transaction, uid: String) = transaction
        .get(profileCollection.document(uid))
        .toObject(ProfileModelImpl::class.java)
        ?: throw NullPointerException("document is null")

    override suspend fun updateMyProfile(profile: ProfileModelImpl) {
        val data = mapOf<String, Any?>(
            "uid" to profile.uid,
            "name" to profile.name,
            "iconUrl" to profile.iconUrl,
        ).toMutableMap()
        profile.message?.run { data["message"] = this }
        profile.locale?.run {
            data["locale"] = mapOf(
                "lang" to lang,
                "region" to region
            )
        }
        profile.checkedAt?.run { data["checkedAt"] = this }
        profile.status?.run { data["status"] = this }

        profileCollection.document(profile.uid)
            .set(data, SetOptions.merge())
            .await()
    }

    override suspend fun checkInCommunity(profile: ProfileModelImpl) {
        val data = mapOf(
            "uid" to profile.uid,
            "name" to profile.name,
            "iconUrl" to profile.iconUrl,
            "checkedAt" to FieldValue.serverTimestamp(),
            "status" to "in"
        ).toMutableMap()
        profile.message?.run { data["message"] = this }
        profile.locale?.run {
            data["locale"] = mapOf(
                "lang" to lang,
                "region" to region
            )
        }

        profileCollection.document(profile.uid)
            .set(data, SetOptions.merge())
            .await()
    }

    override suspend fun checkOutCommunity(uid: String) {
        profileCollection.document(uid)
            .set(
                mapOf("status" to "out"),
                SetOptions.merge()
            )
            .await()
    }

    override suspend fun getCheckedInProfileList() =
        profileCollection
            .whereEqualTo("status", "in")
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(ProfileModelImpl::class.java) }
}