package kr.co.hs.sudoku.datasource.admin

import kr.co.hs.sudoku.model.admin.AdminModel

interface AdminRemoteSource {
    suspend fun getAdminModel(uid: String): AdminModel
}