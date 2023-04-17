package kr.co.hs.sudoku.datasource.user

import kr.co.hs.sudoku.model.user.ProfileModel

interface ProfileRemoteSource {
    suspend fun getProfile(uid: String): ProfileModel
    suspend fun updateMyProfile(profile: ProfileModel)
}