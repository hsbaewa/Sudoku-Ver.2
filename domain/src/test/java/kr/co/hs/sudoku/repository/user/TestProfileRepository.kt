package kr.co.hs.sudoku.repository.user

import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.impl.LocaleEntityImpl
import kr.co.hs.sudoku.model.user.impl.OnlineUserEntityImpl
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl
import java.util.Date

class TestProfileRepository : ProfileRepository {

    private val dummyData = hashMapOf<String, ProfileEntity?>(
        "0" to object : ProfileEntity.UserEntity {
            override val lastCheckedAt: Date = Date()
            override val uid: String = "0"
            override var displayName: String = "user0"
            override var message: String? = "message0"
            override var iconUrl: String? = "https://cdn-icons-png.flaticon.com/512/21/21104.png"
            override val locale: LocaleEntity = LocaleEntityImpl("ko", "kr")
        },
        "1" to object : ProfileEntity.UserEntity {
            override val lastCheckedAt: Date = Date()
            override val uid: String = "1"
            override var displayName: String = "user1"
            override var message: String? = "message1"
            override var iconUrl: String? = null
            override val locale: LocaleEntity = LocaleEntityImpl("ko", "kr")
        },
        "2" to object : ProfileEntity.OnlineUserEntity {
            override val checkedAt: Date = Date()
            override val uid: String = "2"
            override var displayName: String = "user2"
            override var message: String? = "message2"
            override var iconUrl: String? = "https://cdn-icons-png.flaticon.com/512/21/21104.png"
            override val locale: LocaleEntity = LocaleEntityImpl("ko", "kr")
        }
    )

    override suspend fun getProfile(uid: String): ProfileEntity {
        if (uid.isEmpty())
            throw ProfileRepository.ProfileException.EmptyUserId("uid is empty")

        return dummyData.getOrDefault(uid, null)
            ?: throw ProfileRepository.ProfileException.ProfileNotFound("profile not found")
    }

    override suspend fun setProfile(profileEntity: ProfileEntity) {
        if (profileEntity.uid.isEmpty())
            throw ProfileRepository.ProfileException.EmptyUserId("uid is empty")

        dummyData[profileEntity.uid] = profileEntity
    }

    override suspend fun checkIn(profileEntity: ProfileEntity) {
        val profile = dummyData.getOrDefault(profileEntity.uid, null)
            ?: throw ProfileRepository.ProfileException.ProfileNotFound("profile not found")

        if (profile is ProfileEntity.UserEntity) {
            dummyData[profile.uid] = with(profile) {
                OnlineUserEntityImpl(uid, displayName, message, iconUrl, locale, checkedAt = Date())
            }
        }
    }

    override suspend fun checkIn(uid: String) {
        val profile = dummyData.getOrDefault(uid, null)
            ?: throw ProfileRepository.ProfileException.ProfileNotFound("profile not found")

        if (profile is ProfileEntity.UserEntity) {
            dummyData[profile.uid] = with(profile) {
                OnlineUserEntityImpl(uid, displayName, message, iconUrl, locale, checkedAt = Date())
            }
        }
    }

    override suspend fun checkOut(uid: String) {
        val profile = dummyData.getOrDefault(uid, null)
            ?: throw ProfileRepository.ProfileException.ProfileNotFound("profile not found")

        if (profile is ProfileEntity.OnlineUserEntity) {
            dummyData[profile.uid] = with(profile) {
                ProfileEntityImpl(
                    uid,
                    displayName,
                    message,
                    iconUrl,
                    locale,
                    lastCheckedAt = Date()
                )
            }
        }
    }

    override suspend fun getOnlineUserList(): List<ProfileEntity.OnlineUserEntity> =
        dummyData.values.mapNotNull { it as? ProfileEntity.OnlineUserEntity }
}