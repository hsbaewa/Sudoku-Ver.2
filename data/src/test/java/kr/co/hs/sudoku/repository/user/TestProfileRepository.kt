package kr.co.hs.sudoku.repository.user

import com.google.firebase.auth.FirebaseAuth
import kr.co.hs.sudoku.datasource.user.ProfileDataSource
import kr.co.hs.sudoku.datasource.user.ProfileRemoteSource
import kr.co.hs.sudoku.model.user.ProfileEntity

class TestProfileRepository(
    private val dataSource: ProfileDataSource,
    private val remoteSource: ProfileRemoteSource
) : ProfileRepository by ProfileRepositoryImpl(
    dataSource,
    remoteSource,
    FirebaseAuth.getInstance()
) {
    override suspend fun getProfile(): ProfileEntity = getProfile("profile-uid")
}