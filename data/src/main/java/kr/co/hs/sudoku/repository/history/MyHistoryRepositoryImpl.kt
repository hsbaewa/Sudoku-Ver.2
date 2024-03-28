package kr.co.hs.sudoku.repository.history

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kr.co.hs.sudoku.datasource.challenge.ChallengeRecordRemoteSource
import kr.co.hs.sudoku.datasource.logs.LogRemoteSource
import kr.co.hs.sudoku.mapper.RecordMapper.toDomain
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.challenge.ClearTimeRecordModel
import kr.co.hs.sudoku.model.history.HistoryEntity
import kr.co.hs.sudoku.model.logs.LogModel
import kr.co.hs.sudoku.model.logs.impl.BattleClearModelImpl
import kr.co.hs.sudoku.model.logs.impl.ChallengeClearModelImpl
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.RepositoryException
import kr.co.hs.sudoku.usecase.NoErrorUseCase
import kr.co.hs.sudoku.usecase.UseCaseFlow
import kr.co.hs.sudoku.usecase.challenge.GetChallengeUseCase
import java.util.Date
import javax.inject.Inject

class MyHistoryRepositoryImpl
@Inject constructor(
    private val getChallenge: GetChallengeUseCase,
    private val logRemoteSource: LogRemoteSource,
    private val recordRemoteSource: ChallengeRecordRemoteSource,
    private val getCurrentUserProfile: NoErrorUseCase<Unit, ProfileEntity>
) : MyHistoryRepository {

    private fun getRecordFlow(challengeId: String, uid: String): Flow<RankerEntity?> =
        flow<ClearTimeRecordModel?> {
            emit(recordRemoteSource.getRecord(challengeId, uid))
        }.flowOn(
            Dispatchers.IO
        ).catch {
            when (it) {
                is NullPointerException -> emit(null)
                else -> throw it
            }
        }.map { it?.toDomain() }


    private suspend fun LogModel.toDomain(): HistoryEntity? = when (this) {
        is LogModel.BattleClear -> null
        is LogModel.ChallengeClear -> {
            val challengeCreatedAt =
                (getChallenge(challengeId).firstOrNull() as? UseCaseFlow.Result.Success)?.data?.createdAt
            val record = getRecordFlow(challengeId, uid).firstOrNull()
            if (challengeCreatedAt != null && record != null) {
                HistoryEntity.ChallengeClear(
                    this.id,
                    challengeCreatedAt,
                    this.uid,
                    this.challengeId,
                    record.rank,
                    this.createdAt.toDate(),
                    this.record,
                )
            } else null
        }
    }

    private fun createClearHistory(model: LogModel): Flow<HistoryEntity> = flow {
        emit(logRemoteSource.createLog(model))
    }.map {
        logRemoteSource.getLog(it)
    }.catch {
        when (it) {
            is NullPointerException -> throw RepositoryException.NotFoundException("")
            else -> throw it
        }
    }.map {
        it.toDomain() ?: throw RepositoryException.CorruptedException("corrupted model $it")
    }.flowOn(
        Dispatchers.IO
    )

    private suspend fun currentUser() = getCurrentUserProfile(Unit).firstOrNull()?.getSuccessData()

    override suspend fun createChallengeClearHistory(
        challenge: ChallengeEntity,
        record: Long
    ): HistoryEntity {
        val currentUser = currentUser()
            ?: throw MyHistoryRepository.MyHistoryException.RequiredCurrentUserException("current user profile is null")

        if (record <= 0)
            throw RepositoryException.InvalidateParameterException("record is $record")

        val data = ChallengeClearModelImpl().also {
            it.uid = currentUser.uid
            it.challengeId = challenge.challengeId
            it.record = record
        }

        return createClearHistory(data).first()
    }

    override suspend fun createBattleClearHistory(
        battle: BattleEntity,
        record: Long
    ): HistoryEntity {
        val currentUser = currentUser()
            ?: throw MyHistoryRepository.MyHistoryException.RequiredCurrentUserException("current user profile is null")

        if (record <= 0)
            throw RepositoryException.InvalidateParameterException("record is $record")

        val data = BattleClearModelImpl().also {
            it.battleId = battle.id
            it.battleWith = battle.participants.map { it.uid }
            it.uid = currentUser.uid
            it.record = record
        }

        return createClearHistory(data).first()
    }

    override suspend fun getList(createdAt: Date, count: Long): List<HistoryEntity> {
        val currentUser = currentUser()
            ?: throw MyHistoryRepository.MyHistoryException.RequiredCurrentUserException("current user profile is null")

        return flow {
            emitAll(logRemoteSource.getLogs(currentUser.uid, createdAt, count).asFlow())
        }.mapNotNull {
            it.toDomain()
        }.flowOn(
            Dispatchers.IO
        ).toList()
    }

    override suspend fun delete(id: String) {
        if (id.isEmpty())
            throw RepositoryException.EmptyIdException("history id is empty")

        flow {
            val entity = get(id)
            emit(logRemoteSource.deleteLog(entity.id))
        }.flowOn(
            Dispatchers.IO
        ).first()
    }

    override suspend fun get(id: String): HistoryEntity {
        if (id.isEmpty())
            throw RepositoryException.EmptyIdException("history id is empty")

        val currentUser = currentUser()
            ?: throw MyHistoryRepository.MyHistoryException.RequiredCurrentUserException("current user profile is null")

        return flow {
            emit(logRemoteSource.getLog(id))
        }.onEach {
            if (it.uid != currentUser.uid)
                throw MyHistoryRepository.MyHistoryException.NotMineException("history is not mine")
        }.map {
            it.toDomain()
                ?: throw RepositoryException.CorruptedException("can not parse history : $it")
        }.flowOn(
            Dispatchers.IO
        ).first()
    }

}