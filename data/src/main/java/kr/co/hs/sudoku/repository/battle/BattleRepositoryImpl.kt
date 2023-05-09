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
import kr.co.hs.sudoku.model.battle.BattleParticipantModel
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.user.ProfileEntity
import java.util.Date

class BattleRepositoryImpl(
    private val remoteSource2: BattleRemoteSourceImpl = BattleRemoteSourceImpl()
) : BattleRepository {

    override suspend fun createBattle(profile: ProfileEntity, matrix: IntMatrix): BattleEntity {
        val profileModel = profile.toData()
        val battleId = FirebaseFirestore.getInstance().runTransaction {
            val battleModel = BattleModel(profileModel, matrix)
            val battleId = remoteSource2.createBattle(it, battleModel)

            val participant = BattleParticipantModel(profileModel).also { p ->
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
            ?: throw BattleRepository.BattleCreateFailedException("Domain 모델로의 변경 실패")
    }

    override suspend fun getBattle(battleId: String) = remoteSource2.getBattle(battleId)?.toDomain()

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

    override suspend fun joinBattle(battleEntity: BattleEntity, profile: ProfileEntity) {
        if (battleEntity.host == profile.uid)
            return

        FirebaseFirestore.getInstance().runTransaction {
            val battleModel = remoteSource2.getBattle(it, battleEntity.id)
                ?: throw BattleRepository.UnknownBattleException()

            val participantModel = BattleParticipantModel(profile.toData()).apply {
                this.battleId = battleModel.id
                this.matrix = battleModel.startingMatrix
            }

            remoteSource2.setParticipant(it, participantModel)
        }.await()
    }

    override suspend fun getJoinedBattle(profile: ProfileEntity) =
        runCatching {
            FirebaseFirestore.getInstance().runTransaction {
                remoteSource2
                    .getParticipant(it, profile.uid)
                    ?.battleId
                    ?.run { remoteSource2.getBattle(it, this) }
            }.await()
        }
            .getOrNull()
            ?.toDomain()

    override suspend fun readyToBattle(profile: ProfileEntity) {
        FirebaseFirestore.getInstance().runTransaction {
            remoteSource2.setParticipant(it, profile.uid, mapOf("isReady" to true))
        }.await()
    }

    override suspend fun unreadyToBattle(profile: ProfileEntity) {
        FirebaseFirestore.getInstance().runTransaction {
            remoteSource2.setParticipant(it, profile.uid, mapOf("isReady" to false))
        }.await()
    }

    override suspend fun isAllReady(battleEntity: BattleEntity) =
        remoteSource2
            .getParticipantList(battleEntity.id)
            .none { !it.isReady }

    override suspend fun exitBattle(battleEntity: BattleEntity, profile: ProfileEntity) {
        FirebaseFirestore.getInstance().runTransaction {
            val battleModel = remoteSource2.getBattle(it, battleEntity.id)
                ?: throw BattleRepository.UnknownBattleException()
            val participantModel = remoteSource2.getParticipant(it, profile.uid)
                ?: return@runTransaction

            if (battleModel.id == participantModel.battleId)
                remoteSource2.deleteParticipant(it, participantModel)
        }.await()

        val participantList = remoteSource2.getParticipantList(battleEntity.id)
        FirebaseFirestore.getInstance().runTransaction {
            if (participantList.isEmpty()) {
                if (remoteSource2.getBattle(it, battleEntity.id)?.winnerUid == null) {
                    remoteSource2.deleteBattle(it, battleEntity.id)
                }
            } else {
                val participant = participantList.first()
                remoteSource2.updateBattle(
                    it,
                    battleEntity.id,
                    mapOf("hostUid" to participant.uid)
                )
                remoteSource2.setParticipant(
                    it,
                    participant.uid,
                    mapOf("isReady" to true)
                )
            }
        }.await()
    }

    override suspend fun startBattle(battleEntity: BattleEntity, uid: String) {
        val participants = remoteSource2.getParticipantList(battleEntity.id)
        participants.filter { it.uid != uid }.takeIf { it.isEmpty() }
            ?.run { throw Exception("is empty guest users") }

        participants.filter { !it.isReady }.takeIf { it.isNotEmpty() }
            ?.run { throw Exception("not to all ready") }

        FirebaseFirestore.getInstance().runTransaction {
            val battleModel = remoteSource2.getBattle(it, battleEntity.id)
            if (uid != battleModel?.hostUid)
                throw Exception("start only host(${battleEntity.host}) but you are $uid")

            remoteSource2.updateBattle(
                it,
                battleEntity.id,
                mapOf("startedAt" to FieldValue.serverTimestamp())
            )
        }.await()
    }

    override suspend fun startBattle(battleEntity: BattleEntity) {
        startBattle(battleEntity, FirebaseAuth.getInstance().currentUser?.uid!!)
    }

    override suspend fun updateClearRecord(
        battleEntity: BattleEntity,
        profile: ProfileEntity,
        clearTime: Long
    ) {
        FirebaseFirestore.getInstance().runTransaction {
            val participantModel = remoteSource2.getParticipant(it, profile.uid)
                ?: throw BattleRepository.UnknownParticipantException()

            if (participantModel.battleId != battleEntity.id)
                throw Exception("not in battle user")
            if (!participantModel.isReady)
                throw Exception("not to ready user")

            val battleModel = remoteSource2.getBattle(it, battleEntity.id)
                ?: throw BattleRepository.UnknownBattleException()

            val winnerUid = battleModel.winnerUid

            val updateBattleModel = if (winnerUid == null) {
                mapOf(
                    "winnerUid" to profile.uid,
                    "clearTime_${profile.uid}" to clearTime
                )
            } else {
                remoteSource2.getBattleRecord(it, battleEntity.id, winnerUid)
                    ?.let { recordModel ->
                        if (clearTime < recordModel.clearTime) {
                            mapOf(
                                "winnerUid" to profile.uid,
                                "clearTime_${profile.uid}" to clearTime
                            )
                        } else {
                            mapOf("clearTime_${profile.uid}" to clearTime)
                        }
                    }
                    ?: mapOf(
                        "winnerUid" to profile.uid,
                        "clearTime_${profile.uid}" to clearTime
                    )
            }

            remoteSource2.updateBattle(it, battleEntity.id, updateBattleModel)
            remoteSource2.setBattleRecord(
                it,
                battleEntity.id,
                profile.uid,
                profile.asMutableMap().apply { this["clearTime"] = clearTime }
            )

        }.await()
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
                .addSnapshotListener { value, _ ->
                    value?.toObject(BattleParticipantModel::class.java)
                        ?.toDomain()
                        ?.run { changedListener.onChanged(this) }
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
}