package kr.co.hs.sudoku.datasource.user

import kr.co.hs.sudoku.model.user.ProfileModelImpl

interface ProfileRemoteSource {
    suspend fun getProfile(uid: String): ProfileModelImpl
    suspend fun updateMyProfile(profile: ProfileModelImpl)
}