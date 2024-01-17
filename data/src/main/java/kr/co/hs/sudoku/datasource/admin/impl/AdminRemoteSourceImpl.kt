package kr.co.hs.sudoku.datasource.admin.impl

import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.FireStoreRemoteSource
import kr.co.hs.sudoku.datasource.admin.AdminRemoteSource
import kr.co.hs.sudoku.model.admin.AdminModel

class AdminRemoteSourceImpl : FireStoreRemoteSource(), AdminRemoteSource {
    private fun getAdminCollectionRef() = rootDocument.collection("admin")
    override suspend fun getAdminModel(uid: String) = getAdminCollectionRef()
        .document(uid)
        .get()
        .await()
        .toObject(AdminModel::class.java)
        ?: AdminModel()
}