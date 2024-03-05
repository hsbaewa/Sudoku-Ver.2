package kr.co.hs.sudoku.repository.battle

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.datasource.FireStoreRemoteSource
import kr.co.hs.sudoku.datasource.battle.BattleRemoteSource
import kr.co.hs.sudoku.mapper.BattleMapper.toDomain
import kr.co.hs.sudoku.mapper.BattleMapper.toDomain2
import kr.co.hs.sudoku.mapper.DocumentSnapshotMapper.toBattleModel
import kr.co.hs.sudoku.mapper.DocumentSnapshotMapper.toParticipantModel
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.battle.ParticipantEntity
import kr.co.hs.sudoku.repository.TestableRepository

internal class BattleEventRepositoryImpl(
    override val battleId: String,
    private val remoteSource: BattleRemoteSource,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)
) : BattleEventRepository, TestableRepository {

    companion object {
        private const val TAG = "BattleEventRepositoryImpl"

        @Suppress("unused")
        fun log(msg: String) = Log.d(TAG, msg)
    }


    /**
     * firestore
     */
    private fun getBattleDocument() =
        remoteSource.getBattleCollectionRef().document(battleId)

    private fun getParticipantDocument(uid: String) =
        remoteSource.getParticipantCollectionRef().document(uid)

    // battle 이벤트 수신
    private var listenerForBattle: ListenerRegistration? = null

    // battle 이벤트 수신 중인지 여부
    private fun isMonitoringBattle() = listenerForBattle != null

    // battle 이벤트 모니터링 중단
    private fun stopMonitoringBattle() {
        listenerForBattle?.remove()
        listenerForBattle = null

    }

    // battle 이벤트 모니터링 시작
    private fun startListenBattleDocument(
        onDocumentSnapshot: (DocumentSnapshot) -> Unit,
        onError: (Throwable) -> Unit
    ) = getBattleDocument()
        .addSnapshotListener { value, error ->
            error?.run(onError)
                ?: value?.run(onDocumentSnapshot)
                ?: onError(NullPointerException("battle document is null"))
        }
        .let { listenerForBattle = it }


    // participant 이벤트 수신 맵핑(uid:수신기)
    private val listenerMapForParticipants = HashMap<String, ListenerRegistration>()

    // participant 이벤트 수신
    @Suppress("unused")
    private fun getListenerForParticipant(uid: String) =
        listenerMapForParticipants.takeIf { it.containsKey(uid) }?.get(uid)

    // participant 이벤트 수신기 제거
    @Suppress("unused")
    private fun removeListenerForParticipant(uid: String) =
        listenerMapForParticipants.takeIf { it.containsKey(uid) }?.remove(uid)

    // 해당 participant 이벤트를 수신중인지??
    @Suppress("unused")
    private fun isMonitoringParticipant(uid: String) = listenerMapForParticipants.containsKey(uid)


    // partiipant 이벤트 수신 시작
    private fun startListenParticipantDocument(
        uid: String,
        onDocumentSnapshot: (DocumentSnapshot) -> Unit,
        onError: (String, Throwable) -> Unit
    ) = getParticipantDocument(uid)
        .addSnapshotListener { value, error ->
            error?.run { onError(uid, this) }
                ?: value?.run(onDocumentSnapshot)
                ?: onError(uid, NullPointerException("participant document is null"))
        }
        .let { listenerMapForParticipants[uid] = it }


    // participant 모니터 중단
    private fun stopMonitoringParticipant(uid: String) =
        with(listenerMapForParticipants) {
            takeIf { it.containsKey(uid) }
                ?.get(uid)
                ?.run {
                    remove()
                    remove(uid)
                }
        }

    // 모든 participant 모니터 중단
    private fun stopMonitoringParticipantAll() =
        with(listenerMapForParticipants) {
            forEach { it.value.remove() }
            clear()
        }


    /**
     *
     */
    private class BattleEntityFlow {
        val flow = MutableSharedFlow<BattleEntity>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

        fun snapshot() = flow.replayCache.lastOrNull() ?: BattleEntity.Invalid()

        suspend fun update(battleEntity: BattleEntity) = flow.emit(battleEntity.clone())

        private suspend fun BattleEntity.emitToFlow(with: Set<ParticipantEntity>) {
            init(with)
            flow.emit(this)
        }

        @Suppress("unused")
        suspend fun add(participant: ParticipantEntity) = with(snapshot()) {
            participants.toMutableSet()
                .takeIf { it.add(participant) }
                ?.run { clone().emitToFlow(this) }
        }

        @Suppress("unused")
        suspend fun remove(participant: ParticipantEntity) = with(snapshot()) {
            participants.toMutableSet()
                .takeIf { it.remove(participant) }
                ?.run { clone().emitToFlow(this) }
        }

        suspend fun remove(participantUid: String) = with(snapshot()) {
            participants.toMutableSet()
                .takeIf { set -> set.removeIf { it.uid == participantUid } }
                ?.run { clone().emitToFlow(this) }
        }

        suspend fun update(participant: ParticipantEntity) = with(snapshot()) {
            participants.toMutableSet()
                .takeIf { set ->
                    set.find { it.uid == participant.uid }
                        ?.run {
                            set.remove(this)
                            set.add(participant)
                        }
                        ?: false
                }
                ?.run { clone().emitToFlow(this) }
        }
    }

    private val battleEntityFlow = BattleEntityFlow()
    override val battleFlow = battleEntityFlow.flow
    override fun getBattle() = battleEntityFlow.snapshot()
    private val playMutex = Mutex()

    override fun startMonitoring() = synchronized(this) {
        if (isMonitoringBattle())
            throw Exception("already monitoring")

        startListenBattleDocument(
            onChangedBattleDocumentSnapshot,
            onExceptionParseBattleEntity
        )
    }

    // battle document snapshot 이벤트 수신 하여 battle entity 이벤트로 변환
    private val onChangedBattleDocumentSnapshot: (DocumentSnapshot) -> Unit = { documentSnapshot ->
        documentSnapshot.toBattleModel()
            ?.toDomain()
            ?.run(onChangedBattleEntity)
            ?: onExceptionParseBattleEntity.invoke(NullPointerException("parse error"))
    }


    // battle entity 이벤트
    private val onChangedBattleEntity: (BattleEntity) -> Unit = { currentBattle ->
        coroutineScope.launch {

            playMutex.withLock {
                val lastBattle = getBattle()

                when {
                    // battle entity에 있는 참여자 수 변경 시
                    lastBattle.participantSize != currentBattle.participantSize -> {
                        val participants = lastBattle.participants
                        val remoteParticipants = currentBattle.requestParticipants()

                        val uidSet = participants.map { it.uid }.toSet()
                        val remoteUidSet = remoteParticipants.map { it.uid }.toSet()

                        remoteUidSet.subtract(uidSet)
                            .mapNotNull { uid -> remoteParticipants.find { it.uid == uid } }
                            .takeIf { it.isNotEmpty() }
                            ?.forEach {
                                startListenParticipantDocument(
                                    it.uid,
                                    onChangedParticipantDocumentSnapshot,
                                    onExceptionParseParticipantEntity
                                )
                            }

                        uidSet.subtract(remoteUidSet)
                            .mapNotNull { uid -> participants.find { it.uid == uid } }
                            .takeIf { it.isNotEmpty() }
                            ?.forEach {
                                stopMonitoringParticipant(it.uid)
                            }

                        currentBattle.init(remoteParticipants.toSet())
                        battleEntityFlow.update(currentBattle)
                    }

                    currentBattle is BattleEntity.Pending && currentBattle.isGeneratedSudoku -> {
                        val participants = currentBattle.requestParticipants()
                        currentBattle.init(participants.toSet())
                        battleEntityFlow.update(currentBattle)
                    }

                    // pending 에서 playing 으로 넘어간 시점
                    lastBattle is BattleEntity.Pending && currentBattle is BattleEntity.Playing -> {
                        // participant 상태를 playing으로 바꾸기 위해 다시 participant 요청
                        val participants = currentBattle.requestParticipants()
                        currentBattle.init(participants.toSet())

                        battleEntityFlow.update(currentBattle)
                    }


                    // battle entity 종료시?
                    currentBattle is BattleEntity.Invalid -> {
                        battleEntityFlow.update(BattleEntity.Invalid())
                    }

                    else -> {
                        // 직전 까지 알고 있던 참여자 그대로 공유
                        currentBattle.init(lastBattle.participants)
                        battleEntityFlow.update(currentBattle)
                    }
                }

            }
        }
    }


    private suspend fun BattleEntity.requestParticipants() = withContext(Dispatchers.IO) {
        remoteSource.getParticipantList(id)
            .map { it.toDomain2(this@requestParticipants) }
    }


    private val onChangedParticipantDocumentSnapshot: (DocumentSnapshot) -> Unit = { document ->
        document.toParticipantModel()
            ?.takeIf { it.battleId == battleId }
            ?.toDomain2(getBattle())
            ?.run(onChangedParticipantEntity)
    }

    private val onChangedParticipantEntity: (ParticipantEntity) -> Unit = { participantEntity ->
        coroutineScope.launch {
            playMutex.withLock { battleEntityFlow.update(participantEntity) }
        }
    }

    private val onExceptionParseParticipantEntity: (String, Throwable) -> Unit = { uid, _ ->
        stopMonitoringParticipant(uid)

        coroutineScope.launch {
            playMutex.withLock { battleEntityFlow.remove(uid) }
        }
    }

    // battle 모니터링이 닫힘(이벤트 구조체가 올바르지 않은 상태 null 이거나 기타)
    private val onExceptionParseBattleEntity: (Throwable) -> Unit = {
        stopMonitoringParticipantAll()
        stopMonitoringBattle()

        coroutineScope.launch {
            playMutex.withLock { battleEntityFlow.update(BattleEntity.Invalid()) }
        }
    }


    override fun stopMonitoring() {
        stopMonitoringParticipantAll()
        stopMonitoringBattle()

        coroutineScope.cancel()
    }

    override fun setFireStoreRootVersion(versionName: String) {
        val rootDocument = FirebaseFirestore.getInstance()
            .collection("version")
            .document(versionName)

        (remoteSource as FireStoreRemoteSource).rootDocument = rootDocument
    }
}