package kr.co.hs.sudoku.repository.user

import com.google.firebase.auth.FirebaseAuth
import kr.co.hs.sudoku.datasource.user.ProfileDataSource
import kr.co.hs.sudoku.datasource.user.ProfileRemoteSource
import kr.co.hs.sudoku.mapper.ProfileMapper.toDomain
import kr.co.hs.sudoku.model.user.LocaleModel
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.ProfileModelImpl
import javax.inject.Inject

class ProfileRepositoryImpl
@Inject constructor(
    private val dataSource: ProfileDataSource,
    private val remoteSource: ProfileRemoteSource,
    private val firebaseAuth: FirebaseAuth
) : ProfileRepository {

    override suspend fun getProfile(uid: String): ProfileEntity {
        return dataSource.getProfile(uid)?.toDomain().takeIf { it != null }?.run {
            this
        } ?: remoteSource.getProfile(uid)
            .also { dataSource.setProfile(it) }
            .toDomain()
    }

    override suspend fun getProfile(): ProfileEntity =
        getProfile(
            firebaseAuth.currentUser?.uid
                ?: throw ProfileRepository.ProfileException.ProfileNotFound("current user not found")
        )

    override suspend fun setProfile(profileEntity: ProfileEntity) {
        val data = profileEntity.toData()
        remoteSource.updateMyProfile(data)
        dataSource.setProfile(data)
    }

    private fun ProfileEntity.toData() = ProfileModelImpl(
        uid = uid,
        name = displayName,
        message = message,
        iconUrl = iconUrl,
        locale = locale?.run { LocaleModel(lang, region) },
        checkedAt = null,
        status = null
    )

    override suspend fun checkIn(uid: String): ProfileEntity =
        remoteSource.checkInCommunity(uid)
            .also { dataSource.setProfile(it) }
            .toDomain()

    override suspend fun checkOut(uid: String): ProfileEntity =
        remoteSource.checkOutCommunity(uid)
            .also { dataSource.setProfile(it) }
            .toDomain()

    override suspend fun getOnlineUserList() = remoteSource.getCheckedInProfileList()
        .mapNotNull { it.toDomain() as? ProfileEntity.OnlineUserEntity }
}