package kr.co.hs.sudoku.repository.user

import com.google.firebase.firestore.FirebaseFirestore
import kr.co.hs.sudoku.datasource.user.ProfileDataSource
import kr.co.hs.sudoku.datasource.user.impl.ProfileDataSourceImpl
import kr.co.hs.sudoku.datasource.user.impl.ProfileRemoteSourceImpl
import kr.co.hs.sudoku.mapper.ProfileMapper.toDomain
import kr.co.hs.sudoku.model.user.LocaleModel
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.ProfileModelImpl
import kr.co.hs.sudoku.repository.TestableRepository

class ProfileRepositoryImpl(
    private val dataSource: ProfileDataSource = ProfileDataSourceImpl()
) : ProfileRepository, TestableRepository {

    private val remoteSource = ProfileRemoteSourceImpl()

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

    override fun setFireStoreRootVersion(versionName: String) {
        remoteSource.rootDocument = FirebaseFirestore.getInstance()
            .collection("version")
            .document(versionName)
    }

    override suspend fun checkIn(profileEntity: ProfileEntity) =
        remoteSource.checkInCommunity(profileEntity.toData())

    override suspend fun checkIn(uid: String) =
        with(remoteSource) { checkInCommunity(getProfile(uid)) }

    override suspend fun checkOut(uid: String) = remoteSource.checkOutCommunity(uid)

    override suspend fun getOnlineUserList() = remoteSource.getCheckedInProfileList()
        .mapNotNull { it.toDomain() as? ProfileEntity.OnlineUserEntity }
}