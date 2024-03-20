package kr.co.hs.sudoku.datasource.user.impl

import kr.co.hs.sudoku.datasource.user.ProfileDataSource
import kr.co.hs.sudoku.model.user.ProfileModel
import javax.inject.Inject

class ProfileDataSourceImpl
@Inject constructor(
) : ProfileDataSource,
    MutableMap<String, ProfileModel> by HashMap() {
    override fun getProfile(uid: String) = if (containsKey(uid)) get(uid) else null
    override fun setProfile(profile: ProfileModel) {
        this[profile.uid] = profile
    }
}