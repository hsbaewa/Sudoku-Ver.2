package kr.co.hs.sudoku.repository.user

import kr.co.hs.sudoku.model.user.ProfileEntity
import java.util.Date

interface ProfileRepository {
    suspend fun getProfile(uid: String): ProfileEntity
    suspend fun setProfile(profileEntity: ProfileEntity)
    suspend fun check()
    suspend fun getCheckedAt(uid: String): Date
}