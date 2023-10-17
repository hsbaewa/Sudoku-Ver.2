package kr.co.hs.sudoku.datasource.user

import com.google.firebase.firestore.Transaction
import kr.co.hs.sudoku.model.user.ProfileModelImpl

interface ProfileRemoteSource {
    suspend fun getProfile(uid: String): ProfileModelImpl
    fun getProfile(transaction: Transaction, uid: String): ProfileModelImpl
    suspend fun updateMyProfile(profile: ProfileModelImpl)
}