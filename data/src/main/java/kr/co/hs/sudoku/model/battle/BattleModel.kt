package kr.co.hs.sudoku.model.battle

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import kr.co.hs.sudoku.mapper.Mapper.asMutableMap
import kr.co.hs.sudoku.model.user.ProfileModelImpl

@IgnoreExtraProperties
class BattleModel() {
    @get:Exclude
    var id: String? = null
    var hostUid = ""
    var startingMatrix: List<Int>? = null
    var createdAt: Timestamp? = null
    var pendingAt: Timestamp? = null
    var startedAt: Timestamp? = null
    var winnerUid: String? = null
    var participantMaxSize = 2
    var participantSize = 1

    constructor(
        profile: ProfileModelImpl,
        startingMatrix: List<List<Int>>,
        participantMaxSize: Int
    ) : this() {
        this.hostUid = profile.uid
        this.startingMatrix = startingMatrix.flatten()
        this.participantMaxSize = participantMaxSize
        this.participantSize = 1
    }

    fun toFirebaseData() = asMutableMap().also {
        it["createdAt"] = FieldValue.serverTimestamp()
        it["startedAt"] = null
        it["pendingAt"] = null
    }
}