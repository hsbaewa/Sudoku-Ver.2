package kr.co.hs.sudoku.repository.battle

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.battle.impl.BattleRemoteSourceImpl
import kr.co.hs.sudoku.mapper.BattleMapper.toDomain
import kr.co.hs.sudoku.mapper.Mapper.asMutableMap
import kr.co.hs.sudoku.mapper.ProfileMapper.toData
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.battle.BattleModel
import kr.co.hs.sudoku.model.battle.BattleParticipantEntity
import kr.co.hs.sudoku.model.battle.BattleParticipantModel
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.user.ProfileEntity
import org.jetbrains.annotations.TestOnly
import java.util.Date

class BattleRepositoryImpl(
    private val remoteSource2: BattleRemoteSourceImpl = BattleRemoteSourceImpl()
) : BattleRepository {

    override suspend fun createBattle(profile: ProfileEntity, matrix: IntMatrix) =
        createBattle(profile, matrix, 2)

    override suspend fun createBattle(
        profile: ProfileEntity,
        matrix: IntMatrix,
        participantSize: Int
    ): BattleEntity {
        val profileModel = profile.toData()
        var participant = BattleParticipantModel()
        val battleId = FirebaseFirestore.getInstance().runTransaction {
            val battleModel = BattleModel(profileModel, matrix, participantSize)
            val battleId = remoteSource2.createBattle(it, battleModel)

            participant = BattleParticipantModel(profileModel).also { p ->
                p.battleId = battleId
                p.matrix = battleModel.startingMatrix
                p.isReady = true
            }
            remoteSource2.setParticipant(it, participant)

            return@runTransaction battleId
        }.await()

        return battleId
            ?.run { remoteSource2.getBattle(this) }
            ?.toDomain()
            ?.apply { this.addParticipant(participant.toDomain()) }
            ?: throw BattleRepository.BattleCreateFailedException("Domain 모델로의 변경 실패")
    }

    override suspend fun getBattle(battleId: String) =
        remoteSource2.getBattle(battleId)
            ?.toDomain()
            ?.apply { getParticipantList(battleId).forEach { addParticipant(it) } }

    override suspend fun getBattleList(limit: Long) =
        remoteSource2
            .getBattleList(limit, -1)
            .mapNotNull { it.toDomain() }

    override suspend fun getBattleList(limit: Long, lastAt: Date) =
        remoteSource2
            .getBattleList(limit, lastAt.time)
            .mapNotNull { it.toDomain() }

    override suspend fun getBattleListCreatedBy(uid: String) =
        remoteSource2
            .getBattleListCreatedBy(uid)
            .mapNotNull { it.toDomain() }

    override suspend fun getParticipantList(battleId: String) =
        remoteSource2
            .getParticipantList(battleId)
            .map { it.toDomain() }

    override suspend fun getParticipant(uid: String) =
        remoteSource2
            .getParticipant(uid)
            ?.toDomain()

    override suspend fun joinBattle(
        battleEntity: BattleEntity,
        profile: ProfileEntity
    ): BattleEntity? {
        if (battleEntity.host == profile.uid)
            return battleEntity

        return joinBattle(battleEntity.id, profile)
    }

    override suspend fun joinBattle(battleId: String, profile: ProfileEntity): BattleEntity? {
        val participants = getParticipantList(battleId)

        var requestUser = BattleParticipantModel()
        val resultModel = FirebaseFirestore.getInstance().runTransaction { t ->
            val battleModel = remoteSource2.getBattle(t, battleId)
                ?: throw BattleRepository.UnknownBattleException()

            requestUser = BattleParticipantModel(profile.toData()).apply {
                this.battleId = battleModel.id
                this.matrix = battleModel.startingMatrix
            }

            if (battleModel.hostUid == profile.uid)
                return@runTransaction battleModel

            // 이미 참가중
            val isAlreadyJoined = participants.find { it.uid == profile.uid } != null
            if (isAlreadyJoined)
                return@runTransaction battleModel

            if (participants.size >= battleModel.startingParticipants.size)
                throw Exception("battle is full")

            remoteSource2.setParticipant(t, requestUser)
            remoteSource2.updateBattle(
                t,
                battleId,
                mapOf("participantSize" to FieldValue.increment(1))
            )

            battleModel.apply {
                participantSize += 1
            }
        }.await()

        return resultModel
            .toDomain()
            ?.apply {
                participants.forEach { addParticipant(it) }
                addParticipant(requestUser.toDomain())
            }
    }

    override suspend fun getJoinedBattle(uid: String) =
        runCatching {
            FirebaseFirestore.getInstance().runTransaction {
                remoteSource2
                    .getParticipant(it, uid)
                    ?.battleId
                    ?.run { remoteSource2.getBattle(it, this) }
            }.await()
        }
            .getOrNull()
            ?.toDomain()
            ?.apply { getParticipantList(this.id).forEach { addParticipant(it) } }

    override suspend fun readyToBattle(uid: String) {
        FirebaseFirestore.getInstance().runTransaction {
            remoteSource2.setParticipant(it, uid, mapOf("isReady" to true))
        }.await()
    }

    override suspend fun unreadyToBattle(uid: String) {
        FirebaseFirestore.getInstance().runTransaction {
            remoteSource2.setParticipant(it, uid, mapOf("isReady" to false))
        }.await()
    }

    override suspend fun isAllReady(battleEntity: BattleEntity) =
        remoteSource2
            .getParticipantList(battleEntity.id)
            .none { !it.isReady }

    override suspend fun exitBattle(battleEntity: BattleEntity, profile: ProfileEntity) {
        exitBattle(battleEntity, profile.uid)
    }

    override suspend fun exitBattle(battleEntity: BattleEntity, uid: String) {
        FirebaseFirestore.getInstance().runTransaction { t ->
            val battleModel = remoteSource2.getBattle(t, battleEntity.id)
                ?: throw BattleRepository.UnknownBattleException()
            val participantModel = remoteSource2.getParticipant(t, uid)
                ?: return@runTransaction

            if (battleModel.id == participantModel.battleId) {
                remoteSource2.deleteParticipant(t, participantModel)
            }
        }.await()

        val participantList = remoteSource2.getParticipantList(battleEntity.id)
        FirebaseFirestore.getInstance().runTransaction { t ->
            if (participantList.isEmpty()) {
                // 참여자가 비어 있는경우
                if (remoteSource2.getBattle(t, battleEntity.id)?.winnerUid == null) {
                    // winner uid 정보가 없는경우 필요없는 battle 모델이므로 지운다.
                    remoteSource2.deleteBattle(t, battleEntity.id)
                }
            } else {
                // 참여자가 존재하는 경우 아무 참여자나 1명을 host 로 변경하여 준다.
                val participant = participantList.first()
                remoteSource2.updateBattle(
                    t, battleEntity.id,
                    mapOf(
                        "hostUid" to participant.uid,
                        "participantSize" to participantList.size
                    )
                )
                remoteSource2.setParticipant(
                    t, participant.uid,
                    mapOf("isReady" to true)
                )
            }
        }.await()
    }

    @TestOnly
    override suspend fun startBattle(battleEntity: BattleEntity, uid: String) {
        doStartBattle(battleEntity, uid)
    }

    override suspend fun startBattle(battleEntity: BattleEntity) {
        doStartBattle(battleEntity, currentUserUid)
    }

    private suspend fun doStartBattle(battleEntity: BattleEntity, uid: String): BattleEntity {
        val participants = doGetAllReadyParticipantsOrNull(battleEntity.id, uid)

        FirebaseFirestore.getInstance().runTransaction {
            with(remoteSource2) {
                getBattle(it, battleEntity.id)
                    ?.toDomain()
                    ?.run {
                        if (this !is BattleEntity.PendingBattleEntity)
                            throw Exception("battle start only pending status")

                        if (host != uid)
                            throw Exception("start only host(${host}) but you are $uid")

                        val battleInfoForMod =
                            mapOf(
                                "startedAt" to FieldValue.serverTimestamp(),
                                // 모든 참여자를 startingParticipants로 저장하여 시작 했던 멤버를 기억하도록 한다.(나중에 통계때 필요)
                                "startingParticipants" to participants.map { participant -> participant.uid }
                            )
                        updateBattle(it, id, battleInfoForMod)
                    }
                    ?: throw Exception("not found battle uid = $uid")
            }
        }.await()

        return FirebaseFirestore.getInstance().runTransaction {
            remoteSource2.getBattle(it, battleEntity.id)
        }.await()
            ?.toDomain()
            ?.run {
                takeIf { it is BattleEntity.RunningBattleEntity }
                    ?: throw Exception("battle is not running")
            }
            ?: throw Exception("battle not found")
    }

    private suspend fun doGetAllReadyParticipantsOrNull(battleId: String, uid: String) =
        remoteSource2.getParticipantList(battleId)
            .apply {
                when {
                    none { it.uid != uid } -> throw Exception("is empty participants")
                    any { !it.isReady } -> throw Exception("not to all ready")
                }
            }


    private val currentUserUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw Exception("current user is null")

    override suspend fun updateClearRecord(
        battleEntity: BattleEntity,
        profile: ProfileEntity,
        clearTime: Long
    ) {
        if (battleEntity is BattleEntity.WaitingBattleEntity || battleEntity is BattleEntity.PendingBattleEntity)
            throw Exception("battle waiting yet")

        FirebaseFirestore.getInstance().runTransaction { t ->
            val participantModel = remoteSource2.getParticipant(t, profile.uid)
                ?: throw BattleRepository.UnknownParticipantException()

            if (participantModel.battleId != battleEntity.id)
                throw Exception("not in battle user")
            if (!participantModel.isReady)
                throw Exception("not to ready user")

            val battleModel = remoteSource2.getBattle(t, battleEntity.id)
                ?: throw BattleRepository.UnknownBattleException()

            battleModel.winnerUid
                .takeIf {
                    // winner uid 가 존재하는지 확인
                    it != null
                }
                ?.let { winnerUid ->
                    // 이미 존재하는 winner uid 가 유효한지 확인하기 위해 battle record 정보 조회
                    remoteSource2.getBattleRecord(t, battleEntity.id, winnerUid)
                }
                ?.let { winnerRecordModel ->
                    // 유효한 winner uid 의 battle record의 clearTime이 내 clearTime보다 여전히 앞서 있는지 확인
                    winnerRecordModel.takeIf { it.clearTime <= clearTime }
                }
                ?.let {
                    // 이미 winner 가 존재 하는 경우 내 clearTime만 기록
                    val myRecordData =
                        profile.asMutableMap().apply { this["clearTime"] = clearTime }
                    remoteSource2.setBattleRecord(t, battleEntity.id, profile.uid, myRecordData)
                }
                ?: kotlin.run {
                    // 내가 winner 기록보다 빠르거나 winner 정보가 없는 경우 battle 정보에 winner uid 정보로 내 uid 를 업데이트 한다.
                    val winnerUidData = mapOf("winnerUid" to profile.uid)
                    remoteSource2.updateBattle(t, battleEntity.id, winnerUidData)

                    // 이미 winner 가 존재 하는 경우 내 clearTime을 기록
                    val myRecordData =
                        profile.asMutableMap().apply { this["clearTime"] = clearTime }
                    remoteSource2.setBattleRecord(t, battleEntity.id, profile.uid, myRecordData)
                }
        }.await()

        exitBattle(battleEntity, profile)
    }

    override suspend fun getStatistics(uid: String) = remoteSource2.getStatistics(uid).toDomain()

    private val bindBattleMap = HashMap<String, ListenerRegistration>()
    override fun bindBattle(
        battleId: String,
        changedListener: BattleRepository.BattleChangedListener
    ) {
        unbindBattle(battleId)
        bindBattleMap[battleId] =
            remoteSource2.getBattleCollectionRef().document(battleId)
                .addSnapshotListener { value, _ ->
                    value?.toObject(BattleModel::class.java)
                        ?.apply { id = value.id }
                        ?.toDomain()
                        ?.run {
                            changedListener.onChanged(this)
                        }
                }
    }

    override fun unbindBattle(battleId: String) {
        takeIf { bindBattleMap.containsKey(battleId) }
            ?.run { bindBattleMap[battleId]?.remove() }
    }

    private val bindParticipantsMap = HashMap<String, ListenerRegistration>()

    override fun unbindParticipant(battleId: String, uid: String) {
        takeIf { bindParticipantsMap.containsKey(uid) }
            ?.run { bindParticipantsMap[uid]?.remove() }
    }

    override fun bindParticipant(
        battleId: String,
        uid: String,
        changedListener: BattleRepository.ParticipantChangedListener
    ) {
        unbindParticipant(battleId, uid)
        bindParticipantsMap[uid] =
            remoteSource2.getParticipantCollectionRef().document(uid)
                .addSnapshotListener { value, a ->
                    value?.run {
                        toObject(BattleParticipantModel::class.java)
                            ?.toDomain()
                            ?.run { changedListener.onChanged(this) }
                            ?: kotlin.run { changedListener.onChanged(BattleParticipantEntity(id)) }
                    }
                }
    }

    override suspend fun updateParticipantMatrix(uid: String, matrix: List<List<Int>>) {
        FirebaseFirestore.getInstance().runTransaction {
            remoteSource2.setParticipant(
                it,
                uid,
                mapOf("matrix" to matrix.flatten())
            )
        }.await()
    }


    override suspend fun pendingBattle(battleEntity: BattleEntity) {
        doPendingBattle(battleEntity, currentUserUid)
    }

    @TestOnly
    override suspend fun pendingBattle(battleEntity: BattleEntity, uid: String) {
        doPendingBattle(battleEntity, uid)
    }

    private suspend fun doPendingBattle(battleEntity: BattleEntity, uid: String): BattleEntity {
        doGetAllReadyParticipantsOrNull(battleEntity.id, uid)

        FirebaseFirestore.getInstance().runTransaction {
            with(remoteSource2) {
                getBattle(it, battleEntity.id)
                    ?.toDomain()
                    ?.run {
                        if (this !is BattleEntity.WaitingBattleEntity)
                            throw Exception("do pending is only waiting state")

                        if (host != uid)
                            throw Exception("start pending only host(${host}) but you are $uid")

                        val battleInfoForMod =
                            mapOf("pendingAt" to FieldValue.serverTimestamp())
                        updateBattle(it, id, battleInfoForMod)
                    }
                    ?: throw Exception("not found battle uid = $uid")
            }
        }.await()

        return FirebaseFirestore.getInstance().runTransaction {
            remoteSource2.getBattle(it, battleEntity.id)
        }.await()
            ?.toDomain()
            ?.run {
                takeIf { it is BattleEntity.PendingBattleEntity }
                    ?: throw Exception("battle is not pending")
            }
            ?: throw Exception("battle not found")
    }
}