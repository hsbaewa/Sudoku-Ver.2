package kr.co.hs.sudoku.repository.user

import kr.co.hs.sudoku.model.user.ProfileEntity

interface ProfileRepository {
    suspend fun getProfile(uid: String): ProfileEntity
    suspend fun setProfile(profileEntity: ProfileEntity)
    suspend fun checkIn(profileEntity: ProfileEntity)
    suspend fun checkIn(uid: String)
    suspend fun checkOut(uid: String)
    suspend fun getOnlineUserList(): List<ProfileEntity.OnlineUserEntity>
}