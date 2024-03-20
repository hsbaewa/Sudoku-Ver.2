package kr.co.hs.sudoku.repository.user

import kr.co.hs.sudoku.datasource.user.ProfileDataSource
import kr.co.hs.sudoku.datasource.user.ProfileRemoteSource
import kr.co.hs.sudoku.di.DataSourceModule
import kr.co.hs.sudoku.mapper.ProfileMapper.toDomain
import kr.co.hs.sudoku.model.user.LocaleModel
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.ProfileModelImpl
import javax.inject.Inject

class ProfileRepositoryImpl
@Inject constructor(
    @DataSourceModule.ProfileDataSourceQualifier private val dataSource: ProfileDataSource,
    @DataSourceModule.ProfileRemoteSourceQualifier private val remoteSource: ProfileRemoteSource
) : ProfileRepository {

    override suspend fun getProfile(uid: String): ProfileEntity {
        return dataSource.getProfile(uid)?.toDomain().takeIf { it != null }?.run {
            this
        } ?: remoteSource.getProfile(uid)
            .also { dataSource.setProfile(it) }
            .toDomain()
    }

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