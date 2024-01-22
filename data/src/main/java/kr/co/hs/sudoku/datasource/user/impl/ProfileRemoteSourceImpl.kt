package kr.co.hs.sudoku.datasource.user.impl

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.FireStoreRemoteSource
import kr.co.hs.sudoku.datasource.user.ProfileRemoteSource
import kr.co.hs.sudoku.model.user.ProfileModelImpl

class ProfileRemoteSourceImpl : FireStoreRemoteSource(), ProfileRemoteSource {

    private val profileCollection = rootDocument.collection("profile")

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
        profileCollection.document(profile.uid)
            .set(profile, SetOptions.merge())
            .await()
    }

    override suspend fun setUserCheck(uid: String) {
        profileCollection.document(uid)
            .set(
                mapOf("checkedAt" to FieldValue.serverTimestamp()),
                SetOptions.merge()
            )
            .await()
    }

    override suspend fun getUserCheckedAt(uid: String) =
        profileCollection.document(uid)
            .get()
            .await()
            .getTimestamp("checkedAt")?.toDate()
            ?: throw Exception()
}