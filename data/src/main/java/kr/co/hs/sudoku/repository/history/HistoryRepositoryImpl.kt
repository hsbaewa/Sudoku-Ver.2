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
import kotlinx.coroutines.flow.toList
import kr.co.hs.sudoku.datasource.challenge.ChallengeRecordRemoteSource
import kr.co.hs.sudoku.datasource.logs.LogRemoteSource
import kr.co.hs.sudoku.mapper.RecordMapper.toDomain
import kr.co.hs.sudoku.model.challenge.ClearTimeRecordModel
import kr.co.hs.sudoku.model.history.HistoryEntity
import kr.co.hs.sudoku.model.logs.LogModel
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.repository.RepositoryException
import kr.co.hs.sudoku.usecase.UseCaseFlow
import kr.co.hs.sudoku.usecase.challenge.GetChallengeUseCase
import java.util.Date
import javax.inject.Inject

class HistoryRepositoryImpl
@Inject constructor(
    private val getChallenge: GetChallengeUseCase,
    private val logRemoteSource: LogRemoteSource,
    private val recordRemoteSource: ChallengeRecordRemoteSource
) : HistoryRepository {

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


    override suspend fun getList(uid: String, createdAt: Date, count: Long): List<HistoryEntity> {
        if (uid.isEmpty())
            throw RepositoryException.EmptyIdException("uid is empty")

        return flow {
            emitAll(logRemoteSource.getLogs(uid, createdAt, count).asFlow())
        }.mapNotNull {
            it.toDomain()
        }.flowOn(
            Dispatchers.IO
        ).toList()
    }

    override suspend fun get(id: String): HistoryEntity {
        if (id.isEmpty())
            throw RepositoryException.NotFoundException("history id is empty")

        return flow {
            emit(logRemoteSource.getLog(id))
        }.catch {
            when (it) {
                is NullPointerException -> throw RepositoryException.NotFoundException("not found history id : $id")
                else -> throw it
            }
        }.flowOn(
            Dispatchers.IO
        ).map {
            it.toDomain() ?: throw RepositoryException.CorruptedException("corrupted data $it")
        }.first()
    }
}