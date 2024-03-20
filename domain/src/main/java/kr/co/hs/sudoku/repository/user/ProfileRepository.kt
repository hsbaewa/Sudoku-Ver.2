package kr.co.hs.sudoku.repository.user

import kr.co.hs.sudoku.model.user.ProfileEntity

interface ProfileRepository {

    @Throws(ProfileException::class)
    suspend fun getProfile(uid: String): ProfileEntity

    @Throws(ProfileException::class)
    suspend fun setProfile(profileEntity: ProfileEntity)

    @Throws(ProfileException::class)
    suspend fun checkIn(uid: String): ProfileEntity

    @Throws(ProfileException::class)
    suspend fun checkOut(uid: String): ProfileEntity

    @Throws(ProfileException::class)
    suspend fun getOnlineUserList(): List<ProfileEntity.OnlineUserEntity>

    sealed class ProfileException(p0: String?) : Exception(p0) {
        class EmptyUserId(p0: String?) : ProfileException(p0)
        class ProfileNotFound(p0: String?) : ProfileException(p0)
    }
}