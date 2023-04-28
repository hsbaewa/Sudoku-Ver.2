package kr.co.hs.sudoku.datasource.user.impl

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.user.ProfileRemoteSource
import kr.co.hs.sudoku.model.user.ProfileModelImpl

class ProfileRemoteSourceImpl(
    private val profileCollection: CollectionReference
) : ProfileRemoteSource {

    override suspend fun getProfile(uid: String) = profileCollection
        .document(uid)
        .get()
        .await()
        .toObject(ProfileModelImpl::class.java)
        ?: throw NullPointerException("document is null")

    override suspend fun updateMyProfile(profile: ProfileModelImpl) {
        profileCollection.document(profile.uid)
            .set(profile, SetOptions.merge())
            .await()
    }
}