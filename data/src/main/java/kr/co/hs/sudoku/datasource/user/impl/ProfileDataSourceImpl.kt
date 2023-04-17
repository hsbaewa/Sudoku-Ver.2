package kr.co.hs.sudoku.datasource.user.impl

import kr.co.hs.sudoku.datasource.user.ProfileDataSource
import kr.co.hs.sudoku.model.user.ProfileModel

class ProfileDataSourceImpl(
    private val map: HashMap<String, ProfileModel> = HashMap()
) : ProfileDataSource,
    Map<String, ProfileModel> by map {
    override fun getProfile(uid: String) = if (containsKey(uid)) get(uid) else null
    override fun setProfile(profile: ProfileModel) {
        map[profile.uid] = profile
    }

    override fun clearAll() = map.clear()
}