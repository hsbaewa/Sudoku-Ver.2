package kr.co.hs.sudoku.datasource.user

import com.google.firebase.firestore.Transaction
import kr.co.hs.sudoku.model.user.ProfileModel
import kr.co.hs.sudoku.model.user.ProfileModelImpl

interface ProfileRemoteSource {
    suspend fun getProfile(uid: String): ProfileModelImpl
    fun getProfile(transaction: Transaction, uid: String): ProfileModelImpl
    suspend fun updateMyProfile(profile: ProfileModelImpl)
    suspend fun checkInCommunity(profile: ProfileModelImpl)
    suspend fun checkOutCommunity(uid: String)
    suspend fun getCheckedInProfileList(): List<ProfileModel>
}