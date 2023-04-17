package kr.co.hs.sudoku.datasource.user.impl

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.user.ProfileRemoteSource
import kr.co.hs.sudoku.model.user.ProfileModel

class ProfileRemoteSourceImpl(
    private val profileCollection: CollectionReference
) : ProfileRemoteSource {

    override suspend fun getProfile(uid: String) = profileCollection
        .document(uid)
        .get()
        .await()
        .toObject(ProfileModel::class.java)
        ?: throw NullPointerException("document is null")

    override suspend fun updateMyProfile(profile: ProfileModel) {
        profileCollection.document(profile.uid)
            .set(profile, SetOptions.merge())
            .await()
    }
}