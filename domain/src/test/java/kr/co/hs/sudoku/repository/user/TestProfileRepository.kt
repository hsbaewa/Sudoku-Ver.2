package kr.co.hs.sudoku.repository.user

import kr.co.hs.sudoku.data.TestProfileDataSource
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.impl.OnlineUserEntityImpl
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestProfileRepository
@Inject constructor(
    private val dataSource: TestProfileDataSource
) : ProfileRepository {

    override suspend fun getProfile(uid: String): ProfileEntity {
        if (uid.isEmpty())
            throw ProfileRepository.ProfileException.EmptyUserId("uid is empty")

        return dataSource.dummyData.getOrDefault(uid, null)
            ?: throw ProfileRepository.ProfileException.ProfileNotFound("profile not found")
    }

    override suspend fun getProfile(): ProfileEntity =
        throw ProfileRepository.ProfileException.EmptyUserId("uid is empty")

    override suspend fun setProfile(profileEntity: ProfileEntity) {
        if (profileEntity.uid.isEmpty())
            throw ProfileRepository.ProfileException.EmptyUserId("uid is empty")

        dataSource.dummyData[profileEntity.uid] = profileEntity
    }

    override suspend fun checkIn(uid: String): ProfileEntity {
        val profile = dataSource.dummyData.getOrDefault(uid, null)
            ?: throw ProfileRepository.ProfileException.ProfileNotFound("profile not found")

        return if (profile is ProfileEntity.UserEntity) {
            with(profile) {
                OnlineUserEntityImpl(uid, displayName, message, iconUrl, locale, checkedAt = Date())
            }.also { dataSource.dummyData[profile.uid] = it }
        } else {
            profile
        }
    }

    override suspend fun checkOut(uid: String): ProfileEntity {
        val profile = dataSource.dummyData.getOrDefault(uid, null)
            ?: throw ProfileRepository.ProfileException.ProfileNotFound("profile not found")

        return if (profile is ProfileEntity.OnlineUserEntity) {
            with(profile) {
                ProfileEntityImpl(
                    uid,
                    displayName,
                    message,
                    iconUrl,
                    locale,
                    lastCheckedAt = Date()
                )
            }.also { dataSource.dummyData[profile.uid] = it }
        } else {
            profile
        }
    }

    override suspend fun getOnlineUserList(): List<ProfileEntity.OnlineUserEntity> =
        dataSource.dummyData.values.mapNotNull { it as? ProfileEntity.OnlineUserEntity }
}