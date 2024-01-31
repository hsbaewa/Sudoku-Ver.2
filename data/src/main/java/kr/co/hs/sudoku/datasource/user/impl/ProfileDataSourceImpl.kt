package kr.co.hs.sudoku.datasource.user.impl

import kr.co.hs.sudoku.datasource.user.ProfileDataSource
import kr.co.hs.sudoku.model.user.ProfileModelImpl

class ProfileDataSourceImpl(
    private val map: HashMap<String, ProfileModelImpl> = HashMap()
) : ProfileDataSource,
    Map<String, ProfileModelImpl> by map {
    override fun getProfile(uid: String) = if (containsKey(uid)) get(uid) else null
    override fun setProfile(profile: ProfileModelImpl) {
        map[profile.uid] = profile
    }
}