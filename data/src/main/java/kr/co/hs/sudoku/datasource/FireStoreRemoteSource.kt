package kr.co.hs.sudoku.datasource

import com.google.firebase.firestore.FirebaseFirestore

abstract class FireStoreRemoteSource {
    var rootDocument = FirebaseFirestore.getInstance()
        .collection("version")
        .document("v2")
}