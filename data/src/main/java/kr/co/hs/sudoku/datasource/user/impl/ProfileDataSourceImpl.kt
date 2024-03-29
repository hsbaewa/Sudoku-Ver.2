package kr.co.hs.sudoku.datasource.user.impl

import kr.co.hs.sudoku.datasource.user.ProfileDataSource
import kr.co.hs.sudoku.model.user.ProfileModelImpl

class ProfileDataSourceImpl : ProfileDataSource,
    MutableMap<String, ProfileModelImpl> by HashMap() {
    override fun getProfile(uid: String) = if (containsKey(uid)) get(uid) else null
    override fun setProfile(profile: ProfileModelImpl) {
        this[profile.uid] = profile
    }
}