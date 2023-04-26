package kr.co.hs.sudoku.datasource.timer.impl

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.timer.TimeRemoteSource
import kr.co.hs.sudoku.model.timer.ServerTime

class TimeRemoteSourceImpl : TimeRemoteSource {

    override suspend fun getServerTimestamp(): ServerTime? {
        document.set(mapOf("serverTime" to FieldValue.serverTimestamp())).await()
        return document.get().await().toObject(ServerTime::class.java)
    }

    private val document = FirebaseFirestore.getInstance()
        .collection("version")
        .document("v2")
        .collection("time")
        .document("time")
}