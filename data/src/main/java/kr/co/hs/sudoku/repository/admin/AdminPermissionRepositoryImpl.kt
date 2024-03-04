package kr.co.hs.sudoku.repository.admin

import kr.co.hs.sudoku.datasource.admin.AdminRemoteSource
import kr.co.hs.sudoku.model.admin.AdminPermissionEntity
import javax.inject.Inject

class AdminPermissionRepositoryImpl
@Inject constructor(
    private val adminRemoteSource: AdminRemoteSource
) : AdminPermissionRepository {
    override suspend fun getPermission(uid: String) =
        adminRemoteSource.getAdminModel(uid)
            .run {
                AdminPermissionEntity(
                    hasPermissionCreateChallenge = enabledCreateChallenge,
                    hasPermissionAppUpdatePush = enabledAppUpdatePush
                )
            }
}