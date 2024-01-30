package kr.co.hs.sudoku.datasource.user

import kr.co.hs.sudoku.model.user.ProfileModelImpl

interface ProfileDataSource : Map<String, ProfileModelImpl> {
    fun getProfile(uid: String): ProfileModelImpl?
    fun setProfile(profile: ProfileModelImpl)
}