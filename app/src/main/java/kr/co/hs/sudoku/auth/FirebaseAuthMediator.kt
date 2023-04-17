package kr.co.hs.sudoku.auth

import com.google.firebase.auth.FirebaseUser
import kr.co.hs.sudoku.model.user.ProfileEntity

interface FirebaseAuthMediator {
    suspend fun signIn(): FirebaseUser?
    suspend fun getProfile(uid: String): ProfileEntity?
    suspend fun updateProfile(profileEntity: ProfileEntity): ProfileEntity?
}