package kr.co.hs.sudoku.repository.admin

import kr.co.hs.sudoku.model.admin.AdminPermissionEntity

interface AdminPermissionRepository {
    suspend fun getPermission(uid: String): AdminPermissionEntity
}