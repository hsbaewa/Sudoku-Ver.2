package kr.co.hs.sudoku.repository

import com.google.firebase.firestore.FirebaseFirestore
import kr.co.hs.sudoku.datasource.user.ProfileDataSource
import kr.co.hs.sudoku.datasource.user.impl.ProfileDataSourceImpl
import kr.co.hs.sudoku.datasource.user.impl.ProfileRemoteSourceImpl
import kr.co.hs.sudoku.mapper.ProfileMapper.toDomain
import kr.co.hs.sudoku.model.user.LocaleModel
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.ProfileModel
import kr.co.hs.sudoku.repository.user.ProfileRepository

class ProfileRepositoryImpl(
    private val dataSource: ProfileDataSource = ProfileDataSourceImpl()
) : ProfileRepository {

    private val profileCollection = FirebaseFirestore.getInstance()
        .collection("version")
        .document("v2")
        .collection("profile")

    private val remoteSource = ProfileRemoteSourceImpl(profileCollection)

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

    private fun ProfileEntity.toData() = ProfileModel(
        uid = uid,
        name = displayName,
        message = message,
        iconUrl = iconUrl,
        locale = locale?.run { LocaleModel(lang, region) }
    )
}