package kr.co.hs.sudoku.repository.battle

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.FireStoreRemoteSource
import kr.co.hs.sudoku.datasource.battle.BattleRemoteSource
import kr.co.hs.sudoku.datasource.logs.LogRemoteSource
import kr.co.hs.sudoku.datasource.user.ProfileRemoteSource
import kr.co.hs.sudoku.mapper.BattleMapper.toDomain
import kr.co.hs.sudoku.mapper.BattleMapper.toDomain2
import kr.co.hs.sudoku.mapper.Mapper.asMutableMap
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.battle.BattleLeaderBoardEntity
import kr.co.hs.sudoku.model.battle.BattleModel
import kr.co.hs.sudoku.model.battle.BattleParticipantModel
import kr.co.hs.sudoku.model.battle.ParticipantEntity
import kr.co.hs.sudoku.model.logs.impl.BattleClearModelImpl
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.TestableRepository
import kr.co.hs.sudoku.usecase.AutoGenerateSudokuUseCase
import javax.inject.Inject
import kotlin.math.sqrt

@Suppress("SpellCheckingInspection")
class BattleRepositoryImpl
@Inject
constructor(
    private val battleRemoteSource: BattleRemoteSource,
    private val profileRemoteSource: ProfileRemoteSource,
    private val logRemoteSource: LogRemoteSource
) : BattleRepository, TestableRepository {

    override val currentUserUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw Exception("사용자 인증 정보가 없습니다. 게임 진행을 위해서는 먼저 사용자 인증이 필요합니다.")

    /**
     *
     */
    override suspend fun create(matrix: IntMatrix) = doCreateBattle(matrix)

    override suspend fun create(matrix: IntMatrix, participantSize: Int) =
        doCreateBattle(matrix, participantSize)

    private suspend fun doCreateBattle(matrix: IntMatrix, participantSize: Int = 2): BattleEntity {
        val participantModel = FirebaseFirestore.getInstance().runTransaction { t ->
            val profile = profileRemoteSource.getProfile(t, currentUserUid)

            return@runTransaction with(battleRemoteSource) {
                throwIfExistBattle(t)

                val battleModel = BattleModel(profile, matrix, participantSize)
                val participantModel = BattleParticipantModel(profile)

                val battleId = createBattle(t, battleModel)
                setParticipant(t, participantModel.also { p ->
                    p.battleId = battleId
                    p.matrix = matrix.flatten()
                    p.isReady = true
                })

                participantModel
            }
        }.await()

        return participantModel.battleId
            ?.run { battleRemoteSource.getBattle(this) }
            ?.toDomain()
            ?.apply { init(setOf(participantModel.toDomain2(this))) }
            ?: throw Exception("Domain 모델로의 변경 실패")
    }

    // 이미 생성된 게임이 있는지 확인
    private fun BattleRemoteSource.throwIfExistBattle(t: Transaction) {
        val participant = getParticipant(t, currentUserUid) ?: return
        val participantBattleId = participant.battleId ?: return
        val battle = getBattle(t, participantBattleId)?.toDomain() ?: return
        if (battle !is BattleEntity.Closed) {
            throw BattleRepositoryException(
                "이미 생성된 게임(${battle.id})이 있습니다. 생성된 게임을 종료 후 다시 시도 해 주세요.",
                AlreadyHasCreated
            )
        }
    }


    /**
     *
     */
    override suspend fun search(battleId: String) = with(battleRemoteSource) {
        val battle = getBattle(battleId)
            ?.toDomain()
            ?: throw BattleRepositoryException("게임($battleId)이 존재하지 않습니다.", NotFound)

        battle
            .takeIf { it is BattleEntity.Opened }
            ?: throw BattleRepositoryException(
                "게임($battleId)이 대기중이 아닙니다.(${battle.javaClass.simpleName})",
                RequireStatusOpened
            )
    }

    override suspend fun list() = battleRemoteSource
        .getBattleList(10, -1)
        .mapNotNull { it.runCatching { toDomain() }.getOrNull() }
        .filterIsInstance<BattleEntity.Opened>()

    override suspend fun possibleToJoinList() = battleRemoteSource
        .getEmptyBattleList(10, -1)
        .mapNotNull { it.runCatching { toDomain() }.getOrNull() }
        .filterIsInstance<BattleEntity.Opened>()

    override suspend fun getParticipants(battleEntity: BattleEntity) {
        val list = battleRemoteSource.getParticipantList(battleEntity.id)

        val participantEntities = FirebaseFirestore.getInstance().runTransaction { t ->
            list.map {
                it.toDomain2(battleEntity).run {
                    if (this is ParticipantEntity.Playing) {
                        battleRemoteSource.getBattleRecord(t, battleEntity.id, this.uid)
                            ?.run { toCleared(clearTime) }
                            ?: this
                    } else this
                }
            }
        }.await()

        battleEntity.init(participantEntities.toSet())
    }


    override suspend fun searchWithParticipants(battleId: String) =
        search(battleId).also { getParticipants(it) }

    /**
     *
     */
    override suspend fun isParticipating(): Boolean =
        FirebaseFirestore.getInstance().runTransaction { t ->
            return@runTransaction battleRemoteSource.isParticipating(t)
        }.await()

    private fun BattleRemoteSource.isParticipating(t: Transaction) =
        runCatching { getParticipating(t) }
            .getOrNull()
            ?.takeIf { it !is BattleEntity.Closed } != null

    override suspend fun getParticipating(): BattleEntity =
        FirebaseFirestore.getInstance().runTransaction { t ->
            return@runTransaction battleRemoteSource.getParticipating(t)
        }.await()

    private fun BattleRemoteSource.getParticipating(t: Transaction): BattleEntity {
        val participant =
            getParticipant(t, currentUserUid) ?: throw BattleRepositoryException(
                "참여중인 게임이 없습니다.",
                NotFound
            )

        val battleId = participant.battleId
            ?: throw Exception("참여 중인 게임 정보가 올바르지 않습니다.(battle id is null)")

        val battle = getBattle(t, battleId)
            ?: throw BattleRepositoryException("참여 중인 게임이 종료 되었습니다.", AlreadyHasClosed)

        return battle.toDomain()
    }

    /**
     *
     */
    override suspend fun join(battleId: String): BattleEventRepository {
        doJoinBattle(battleId)
        return BattleEventRepositoryImpl(battleId, battleRemoteSource)
    }

    override suspend fun getEventRepository(battleId: String): BattleEventRepository =
        BattleEventRepositoryImpl(battleId, battleRemoteSource)

    private suspend fun doJoinBattle(battleId: String) {
        FirebaseFirestore.getInstance().runTransaction { t ->
            with(battleRemoteSource) {
                val battle = getBattle(t, battleId)
                    ?: throw BattleRepositoryException("유효 하지 않은 게임($battleId)입니다.", NotFound)

                getParticipant(t, currentUserUid)
                    ?.battleId
                    ?.also {
                        if (it == battle.id) {
                            throw BattleRepositoryException(
                                "참여 하려는 게임(${it})에 이미 참가 중 입니다.",
                                AlreadyHasJoined
                            )
                        } else {
//                            throw Exception("다른 게임(${it})에 참가 중 입니다. 참가 중인 게임을 종료 후 다시 시도 해 주세요.")
                        }
                    }


                val battleEntity = when (val entity = battle.toDomain()) {
                    is BattleEntity.Opened -> entity
                    is BattleEntity.Closed ->
                        throw BattleRepositoryException(
                            "이미 클리어 된 게임($battleId)입니다.",
                            AlreadyHasCleared
                        )

                    is BattleEntity.Pending, is BattleEntity.Playing ->
                        throw BattleRepositoryException(
                            "이미 진행 중인 게임($battleId)입니다.",
                            AlreadyHasStarted
                        )

                    is BattleEntity.Invalid ->
                        throw Exception("유효 하지 않은 게임($battleId)입니다.(mapping error)")
                }

                if (battleEntity.participantSize >= battleEntity.maxParticipants)
                    throw BattleRepositoryException(
                        "게임($battleId)의 참여자가 ${battleEntity.participantSize}/${battleEntity.maxParticipants}로 이미 가득 찼습니다.",
                        AlreadyFull
                    )

                setParticipant(t, BattleParticipantModel(currentUserUid).also {
                    it.update(profileRemoteSource.getProfile(t, currentUserUid))
                    it.battleId = battleId
                    it.matrix = battle.startingMatrix
                })

                updateBattle(t, battleId, mapOf("participantSize" to FieldValue.increment(1)))
            }
        }.await()
    }


    /**
     *
     */
    override suspend fun ready() = doChangeReadyStatus(true)
    private suspend fun doChangeReadyStatus(isReady: Boolean, uid: String = currentUserUid) {
        FirebaseFirestore.getInstance().runTransaction { t ->
            with(battleRemoteSource) {
                val participant = getParticipant(t, uid)
                    ?: throw BattleRepositoryException("현재 어떠한 방에도 참여 중이지 않습니다.", RequireJoinFirst)

                if (participant.isReady == isReady) {
                    throw BattleRepositoryException(
                        "이미 준비 상태가 ${participant.isReady} 상태입니다.",
                        if (isReady) AlreadyHasReady else AlreadyHasUnReady
                    )
                }

                val battleId = participant.battleId
                    ?: throw Exception("참여 중인 정보가 올바르지 않습니다.")

                getBattle(t, battleId)
                    ?.run {
                        if (hostUid == participant.uid) {
                            throw BattleRepositoryException(
                                "host($hostUid)는 ready 상태 변경이 불가 합니다.",
                                OnlyParticipant
                            )
                        }
                    }
                    ?: throw BattleRepositoryException("참여 중인 방이 삭제 되었거나 정보가 올바르지 않습니다.", NotFound)

                setParticipant(t, uid, mapOf("isReady" to isReady))
            }
        }.await()
    }

    override suspend fun unready() = doChangeReadyStatus(false)


    /**
     *
     */
    override suspend fun pendingStart() = doPendingStart()

    private suspend fun doPendingStart() {
        val battle = with(getParticipating()) {
            if (this !is BattleEntity.Opened)
                throw BattleRepositoryException("방이 게임을 시작 할 수 있는 상태가 아닙니다.", RequireStatusOpened)

            if (host != currentUserUid) {
                throw BattleRepositoryException("오직 방장(${host})만이 게임을 시작 할 수 있습니다.", OnlyHost)
            }

            this
        }

        with(battleRemoteSource.getParticipantList(battle.id)) {
            when {
                none { it.uid != currentUserUid } -> throw BattleRepositoryException(
                    "참여자가 아무도 없습니다.",
                    EmptyParticipant
                )

                any { !it.isReady } -> throw BattleRepositoryException(
                    "아직 모든 참여자가 준비가 되어 있지 않습니다.",
                    RequireReadyAllUsers
                )

                else -> battleRemoteSource.updateBattle(
                    battle.id,
                    mapOf("pendingAt" to FieldValue.serverTimestamp())
                )
            }

            coroutineScope {

                joinAll(
                    *map {
                        launch {
                            val matrix = CustomMatrix(battle.startingMatrix)
                            AutoGenerateSudokuUseCase(matrix.boxSize, matrix.boxCount, matrix)
                                .invoke()
                                .last()
                                .toValueTable()
                                .run {
                                    battleRemoteSource.setParticipant(
                                        it.uid,
                                        mapOf("matrix" to this.flatten())
                                    )
                                }
                        }
                    }.toTypedArray()
                )
            }

            battleRemoteSource.updateBattle(
                battle.id,
                mapOf("isGeneratedSudoku" to true)
            )

        }
    }

    override suspend fun start() = doStart()

    private suspend fun doStart() {
        val battleId = with(getParticipating()) {
            if (this !is BattleEntity.Pending)
                throw BattleRepositoryException(
                    "방이 게임을 시작 할 수 있는 상태가 아닙니다. 먼저 pending을 호출하세요.",
                    RequireStatusOpened
                )

            if (host != currentUserUid) {
                throw BattleRepositoryException("오직 방장(${host})만이 게임을 시작 할 수 있습니다.", OnlyHost)
            }

            id
        }

        with(battleRemoteSource.getParticipantList(battleId)) {
            when {
                none { it.uid != currentUserUid } -> throw BattleRepositoryException(
                    "참여자가 아무도 없습니다.",
                    EmptyParticipant
                )

                any { !it.isReady } -> throw BattleRepositoryException(
                    "아직 모든 참여자가 준비가 되어 있지 않습니다.",
                    RequireReadyAllUsers
                )

                else -> battleRemoteSource.updateBattle(
                    battleId,
                    mapOf(
                        "startedAt" to FieldValue.serverTimestamp(),
                        // 모든 참여자를 startingParticipants로 저장하여 시작 했던 멤버를 기억하도록 한다.(나중에 통계때 필요)
                        "startingParticipants" to this.map { participant -> participant.uid }
                    )
                )
            }
        }
    }


    /**
     *
     */
    override suspend fun updateMatrix(row: Int, column: Int, value: Int) {
        FirebaseFirestore.getInstance().runTransaction { t ->
            with(battleRemoteSource) {
                getParticipant(t, currentUserUid)
                    ?.let { participant ->

                        val battle = participant.battleId
                            ?.let { battleId -> getBattle(t, battleId) }
                            ?.toDomain()

                        if (battle !is BattleEntity.Playing) {
                            throw BattleRepositoryException(
                                "게임이 아직 시작 되지 않았습니다.",
                                RequireStatusStart
                            )
                        }

                        battle
                            .startingMatrix[row][column]
                            .takeIf { it == 0 }
                            ?.run {
                                participant.matrix?.run {
                                    val columnCount = sqrt(size.toDouble()).toInt()
                                    toMatrix(columnCount)
                                } ?: emptyList()
                            }
                            ?.also { it[row][column] = value }
                            ?.flatten()
                            ?: throw Exception("변경이 불가능한 셀입니다.")
                    }
                    ?.let { setParticipant(t, currentUserUid, mapOf("matrix" to it)) }
            }
        }.await()
    }

    private fun List<Int>.toMatrix(columnCount: Int) = List(columnCount) { row ->
        MutableList(columnCount) { column -> this[(row * columnCount) + column] }
    }

    /**
     *
     */
    override suspend fun clear(record: Long) {
        val battle = getParticipating()
        getParticipants(battle)

        if (battle !is BattleEntity.Playing && battle !is BattleEntity.Closed) {
            throw BattleRepositoryException(
                "아직 게임(${battle.id})이 시작 되지 않았습니다. 게임이 시작 된 이후에 기록을 저장 할 수 있습니다.",
                RequireStatusStart
            )
        }

        FirebaseFirestore.getInstance().runTransaction { t ->
            with(battleRemoteSource) {
                val recordModel = getBattleRecord(t, battle.id, currentUserUid)
                if (recordModel != null) {
                    throw BattleRepositoryException(
                        "이미 ${recordModel.clearTime}ms로 클리어 한 기록이 있습니다.",
                        AlreadyHasCleared
                    )
                }

                val profile = profileRemoteSource.getProfile(t, currentUserUid)

                when (battle) {
                    is BattleEntity.Closed -> {
                        val winnerClearTime =
                            getBattleRecord(t, battle.id, battle.winner)?.clearTime
                        if (winnerClearTime == null || winnerClearTime > record) {
                            updateBattle(t, battle.id, mapOf("winnerUid" to currentUserUid))
                        }
                    }

                    is BattleEntity.Playing -> {
                        updateBattle(t, battle.id, mapOf("winnerUid" to currentUserUid))
                    }

                    else -> {
                        throw BattleRepositoryException(
                            "아직 게임(${battle.id})이 시작 되지 않았습니다. 게임이 시작 된 이후에 기록을 저장 할 수 있습니다.",
                            RequireStatusStart
                        )
                    }
                }

                setBattleRecord(
                    t,
                    battle.id,
                    currentUserUid,
                    profile.asMutableMap().apply { this["clearTime"] = record }
                )

                setParticipant(t, currentUserUid, mapOf("clearTime" to record))

//                deleteParticipant(t, currentUserUid)

            }
        }.await()

        logRemoteSource.createLog(
            BattleClearModelImpl().also {
                it.battleId = battle.id
                it.battleWith = battle.participants.map { it.uid }
                it.uid = currentUserUid
                it.record = record
            }
        )
    }

    override suspend fun exit() = doExitBattle()

    private suspend fun doExitBattle(
        battleParamId: String? = null,
        uid: String = currentUserUid
    ) {
        val battleId = FirebaseFirestore.getInstance().runTransaction { t ->
            with(battleRemoteSource) {
                val participant = getParticipant(t, uid)
                val battleId = battleParamId
                    ?: participant?.battleId
                    ?: throw BattleRepositoryException(
                        "게임에 참여 중이지 않아서 종료가 할 수 없습니다.",
                        RequireJoinFirst
                    )

                val battleModel = getBattle(t, battleId)

                if (battleModel == null || battleModel.id == battleId) {
                    participant?.run { deleteParticipant(t, this) }
                }

                battleId
            }
        }.await()

        // TODO : 외부에서 리스트 호출 없이 transaction 안에서 그냥 확인 하도록 수정 요망
        val participantList = battleRemoteSource.getParticipantList(battleId)

        FirebaseFirestore.getInstance().runTransaction { t ->
            participantList.takeIf { it.isNotEmpty() }
                ?.run {
                    // 참여자가 존재하는 경우 아무 참여자나 1명을 host 로 변경하여 준다.
                    val participant = participantList.first()
                    with(battleRemoteSource) {
                        val modifyBattle = mapOf(
                            "hostUid" to participant.uid,
                            "participantSize" to participantList.size
                        )
                        updateBattle(t, battleId, modifyBattle)
                        setParticipant(t, participant.uid, mapOf("isReady" to true))
                    }
                }
                ?: battleRemoteSource.run {
                    // 참여자가 비어 있는경우
                    getBattle(t, battleId)
                        ?.takeIf { it.winnerUid == null }
                        // winner uid 정보가 없는경우 필요없는 battle 모델이므로 지운다.
                        ?.run { deleteBattle(t, battleId) }
                }

        }.await()
    }

    override suspend fun kick(uid: String) = doKickBattle(uid = uid)
    private suspend fun doKickBattle(battleParamId: String? = null, uid: String) {
        val battleId = FirebaseFirestore.getInstance().runTransaction { t ->
            with(battleRemoteSource) {
                val participant = getParticipant(t, uid)
                val battleId = battleParamId
                    ?: participant?.battleId
                    ?: throw BattleRepositoryException(
                        "게임에 참여 중이지 않아서 종료가 할 수 없습니다.",
                        RequireJoinFirst
                    )

                val battleModel = getBattle(t, battleId)
                if (battleModel == null || battleModel.id == battleId) {
                    participant?.run { deleteParticipant(t, this) }
                }

                if (currentUserUid != uid && currentUserUid != battleModel?.hostUid) {
                    throw BattleRepositoryException("오직 방장만 강퇴가 가능합니다.", OnlyHost)
                }

                if (battleModel?.pendingAt != null) {
                    throw BattleRepositoryException("이미 게임이 시작되었습니다.", AlreadyHasStarted)
                }

                battleId
            }
        }.await()

        // TODO : 외부에서 리스트 호출 없이 transaction 안에서 그냥 확인 하도록 수정 요망
        val participantList = battleRemoteSource.getParticipantList(battleId)

        FirebaseFirestore.getInstance().runTransaction { t ->
            participantList.takeIf { it.isNotEmpty() }
                ?.run {
                    // 참여자가 존재하는 경우 아무 참여자나 1명을 host 로 변경하여 준다.
                    val participant = participantList.first()
                    with(battleRemoteSource) {
                        val modifyBattle = mapOf(
                            "hostUid" to participant.uid,
                            "participantSize" to participantList.size
                        )
                        updateBattle(t, battleId, modifyBattle)
                        setParticipant(t, participant.uid, mapOf("isReady" to true))
                    }
                }
                ?: battleRemoteSource.run {
                    // 참여자가 비어 있는경우
                    getBattle(t, battleId)
                        ?.takeIf { it.winnerUid == null }
                        // winner uid 정보가 없는경우 필요없는 battle 모델이므로 지운다.
                        ?.run { deleteBattle(t, battleId) }
                }

        }.await()
    }

    /**
     *
     */
    override suspend fun getStatistics() = getStatistics(currentUserUid)
    override suspend fun getStatistics(uid: String) =
        battleRemoteSource.getStatistics(uid).toDomain()


    sealed interface ErrorType
    object AlreadyHasCreated : ErrorType
    object AlreadyHasJoined : ErrorType
    object AlreadyHasCleared : ErrorType
    object AlreadyHasStarted : ErrorType
    object AlreadyHasReady : ErrorType
    object AlreadyHasUnReady : ErrorType
    object AlreadyHasClosed : ErrorType
    object AlreadyFull : ErrorType
    object NotFound : ErrorType
    object RequireReadyAllUsers : ErrorType
    object RequireStatusOpened : ErrorType
    object RequireJoinFirst : ErrorType
    object OnlyParticipant : ErrorType
    object OnlyHost : ErrorType
    object EmptyParticipant : ErrorType
    object RequireStatusStart : ErrorType


    class BattleRepositoryException(message: String?, val type: ErrorType) : Exception(message)

    override fun setFireStoreRootVersion(versionName: String) {
        val rootDocument = FirebaseFirestore.getInstance()
            .collection("version")
            .document(versionName)
        (profileRemoteSource as FireStoreRemoteSource).rootDocument = rootDocument
        (battleRemoteSource as FireStoreRemoteSource).rootDocument = rootDocument
        (logRemoteSource as FireStoreRemoteSource).rootDocument = rootDocument
    }


    override suspend fun getLeaderBoard(limit: Long) = with(battleRemoteSource) {
        registerLeaderBoard(currentUserUid)
        var current: BattleLeaderBoardEntity? = null
        this.getLeaderBoard(limit)
            .mapIndexed { idx, item ->
                BattleLeaderBoardEntity(
                    item.uid,
                    item.playCount,
                    item.winCount,
                    idx.toLong() + 1
                )
            }
            .onEach {
                if (it.playCount == current?.playCount && it.winCount == current?.winCount) {
                    current?.ranking?.run { it.ranking = this }
                } else {
                    current = it
                }
            }
            .let {
                List(limit.toInt()) { idx ->
                    it.runCatching { get(idx) }
                        .getOrElse { BattleLeaderBoardEntity("", 0, 0, idx.toLong() + 1) }
                }
            }
    }

    override suspend fun getLeaderBoard(uid: String): BattleLeaderBoardEntity {
        val statistics = battleRemoteSource.getStatistics(uid)
        return battleRemoteSource.runCatching {
            getLeaderBoardModel(uid).run {
                BattleLeaderBoardEntity(
                    uid,
                    statistics.playCount,
                    statistics.winCount,
                    ranking
                )
            }
        }.getOrElse {
            BattleLeaderBoardEntity(
                uid,
                statistics.playCount,
                statistics.winCount,
                0
            )
        }
    }

    override suspend fun syncLeaderBoard() {
        battleRemoteSource.registerLeaderBoard(currentUserUid)
    }
}