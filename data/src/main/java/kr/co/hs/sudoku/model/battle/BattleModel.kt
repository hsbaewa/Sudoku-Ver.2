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

    // battle 모델에도 실시간 참여자 정보를 알고 있어야 스냅샷 이벤트에서 participant 를 매번 쿼리할 필요가 없어진다.
    var participantSize = 1

    // 시작 시점의 참여자 정보 저장(승률 정보 통계 낼때 필요하다. 전체 게임 진행 횟수)
    var startingParticipants: List<String?> = emptyList()

    constructor(
        profile: ProfileModelImpl,
        startingMatrix: List<List<Int>>,
        participantMaxSize: Int
    ) : this() {
        this.hostUid = profile.uid
        this.startingMatrix = startingMatrix.flatten()
        this.participantSize = 1
        this.startingParticipants = List(participantMaxSize) { null }
    }

    fun toFirebaseData() = asMutableMap().also {
        it["createdAt"] = FieldValue.serverTimestamp()
        it["startedAt"] = null
        it["pendingAt"] = null
    }
}