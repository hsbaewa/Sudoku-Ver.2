package kr.co.hs.sudoku.datasource.timer.impl

import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.FireStoreRemoteSource
import kr.co.hs.sudoku.datasource.timer.TimeRemoteSource
import kr.co.hs.sudoku.model.timer.ServerTime
import javax.inject.Inject

class TimeRemoteSourceImpl
@Inject constructor() : FireStoreRemoteSource(), TimeRemoteSource {

    override suspend fun getServerTimestamp(): ServerTime? {
        document.set(mapOf("serverTime" to FieldValue.serverTimestamp())).await()
        return document.get().await().toObject(ServerTime::class.java)
    }

    private val document = rootDocument.collection("time").document("time")
}