package kr.co.hs.sudoku.repository.admin

import kr.co.hs.sudoku.datasource.admin.AdminRemoteSource
import kr.co.hs.sudoku.datasource.admin.impl.AdminRemoteSourceImpl
import kr.co.hs.sudoku.model.admin.AdminPermissionEntity

class AdminPermissionRepositoryImpl(
    private val adminRemoteSource: AdminRemoteSource = AdminRemoteSourceImpl()
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