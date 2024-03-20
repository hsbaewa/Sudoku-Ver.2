package kr.co.hs.sudoku.datasource.user

import kr.co.hs.sudoku.model.user.ProfileModel

interface ProfileDataSource : Map<String, ProfileModel> {
    fun getProfile(uid: String): ProfileModel?
    fun setProfile(profile: ProfileModel)
}