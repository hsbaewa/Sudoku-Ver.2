package kr.co.hs.sudoku.feature.user

import kotlinx.coroutines.flow.Flow
import kr.co.hs.sudoku.model.user.ProfileEntity

interface Authenticator {
    fun signIn(): Flow<ProfileEntity>
    fun getProfile(): Flow<ProfileEntity>
    fun checkIn(): Flow<ProfileEntity>
    fun checkOut(): Flow<ProfileEntity>
    fun updateProfile(profileEntity: ProfileEntity): Flow<ProfileEntity>

    class RequireSignIn(message: String?) : Exception(message)
}