package kr.co.hs.sudoku.repository.challenge

import kotlinx.coroutines.Dispatchers
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
import kr.co.hs.sudoku.datasource.challenge.ChallengeRemoteSource
import kr.co.hs.sudoku.datasource.logs.LogRemoteSource
import kr.co.hs.sudoku.mapper.ChallengeMapper.toDomain
import kr.co.hs.sudoku.mapper.RecordMapper.toDomain
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.challenge.ChallengeModel
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.model.challenge.ClearTimeRecordModel
import kr.co.hs.sudoku.model.challenge.ReserveRecordModel
import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.LocaleModel
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.RepositoryException
import kr.co.hs.sudoku.usecase.NoErrorUseCase
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.sqrt

class ChallengeRepositoryImpl
@Inject constructor(
    private val challengeRemoteSource: ChallengeRemoteSource,
    private val recordRemoteSource: ChallengeRecordRemoteSource,
    private val logRemoteSource: LogRemoteSource,
    private val getCurrentUserProfile: NoErrorUseCase<Unit, ProfileEntity>
) : ChallengeRepository {

    private fun ChallengeEntity.toData() = ChallengeModel(
        boxSize = sqrt(matrix.size.toDouble()).toInt(),
        boxCount = sqrt(matrix.size.toDouble()).toInt(),
        rowCount = matrix.size,
        columnCount = if (matrix.isNotEmpty()) matrix[0].size else 0,
        matrix = matrix.flatten()
    ).apply { id = challengeId }

    private fun RankerEntity.toData() = ClearTimeRecordModel(
        uid = uid,
        name = displayName,
        message = message,
        iconUrl = iconUrl,
        locale = locale?.toData(),
        clearTime = clearTime
    )

    private fun LocaleEntity?.toData() = LocaleModel(
        this?.lang ?: Locale.getDefault().language,
        this?.region ?: Locale.getDefault().country
    )

    private suspend fun currentUser() = getCurrentUserProfile(Unit).firstOrNull()?.getSuccessData()

    override suspend fun getChallenges(createdAt: Date, count: Long): List<ChallengeEntity> {
        val currentUser = currentUser()
            ?: throw ChallengeRepository.ChallengeException.RequiredCurrentUserException("")

        return flow {
            emitAll(challengeRemoteSource.getChallenges(createdAt, count).asFlow())
        }.mapNotNull {
            it.toDomain()
        }.onEach {
            recordRemoteSource.getChallengeMetadata(it, currentUser.uid)
        }.flowOn(
            Dispatchers.IO
        ).toList()
    }

    override suspend fun getChallenge(id: String): ChallengeEntity {
        if (id.isEmpty())
            throw RepositoryException.EmptyIdException("challenge id is null")

        val currentUser = currentUser()
            ?: throw ChallengeRepository.ChallengeException.RequiredCurrentUserException("")

        return flow {
            emit(challengeRemoteSource.getChallenge(id))
        }.catch {
            when (it) {
                is NullPointerException ->
                    throw RepositoryException.NotFoundException(it.message)

                else -> throw it
            }
        }.map {
            it.toDomain()
                ?: throw RepositoryException.CorruptedException("challenge id : $id")
        }.onEach {
            recordRemoteSource.getChallengeMetadata(it, currentUser.uid)
        }.flowOn(
            Dispatchers.IO
        ).first()
    }

    override suspend fun createChallenge(entity: ChallengeEntity): ChallengeEntity {
        return flow {
            entity.createdAt
                ?.let { emit(challengeRemoteSource.createChallenge(entity.toData(), it)) }
                ?: emit(challengeRemoteSource.createChallenge(entity.toData()))
        }.flowOn(
            Dispatchers.IO
        ).map {
            getChallenge(it)
        }.first()
    }

    override suspend fun removeChallenge(challengeId: String): Boolean {
        if (challengeId.isEmpty())
            throw RepositoryException.EmptyIdException("")

        return flow {
            emit(challengeRemoteSource.removeChallenge(challengeId))
        }.flowOn(
            Dispatchers.IO
        ).first()
    }

    override suspend fun getRecords(challengeId: String): List<RankerEntity> {
        if (challengeId.isEmpty())
            throw RepositoryException.EmptyIdException("")

        return recordRemoteSource.getRecords(challengeId, 10)
            .asFlow()
            .flowOn(
                Dispatchers.IO
            )
            .map {
                it.toDomain()
            }
            .toList()
    }

    override suspend fun getRecord(challengeId: String): RankerEntity {
        if (challengeId.isEmpty())
            throw RepositoryException.EmptyIdException("")

        val currentUser = currentUser()
            ?: throw ChallengeRepository.ChallengeException.RequiredCurrentUserException("")

        return flow {
            emit(recordRemoteSource.getRecord(challengeId, currentUser.uid))
        }.flowOn(
            Dispatchers.IO
        ).map {
            it.toDomain()
        }.first()
    }

    override suspend fun putRecord(challengeId: String, clearRecord: Long): Boolean {
        if (challengeId.isEmpty())
            throw RepositoryException.EmptyIdException("")

        if (clearRecord <= 0)
            throw RepositoryException.InvalidateParameterException("clear record is $clearRecord")

        val currentUser = currentUser()
            ?: throw ChallengeRepository.ChallengeException.RequiredCurrentUserException("")

        val data = RankerEntity(currentUser, clearRecord).toData()

        return flow {
            emit(recordRemoteSource.setRecord(challengeId, data))
        }.flowOn(
            Dispatchers.IO
        ).first()
    }

    override suspend fun putReserveRecord(challengeId: String): Boolean {
        if (challengeId.isEmpty())
            throw RepositoryException.EmptyIdException("")

        val currentUser = currentUser()
            ?: throw ChallengeRepository.ChallengeException.RequiredCurrentUserException("")

        val data = ReserveRecordModel(currentUser.uid, "", null, null, null)

        return flow {
            emit(recordRemoteSource.setRecord(challengeId, data))
        }.flowOn(
            Dispatchers.IO
        ).first()
    }

    override suspend fun deleteRecord(challengeId: String): Boolean {
        if (challengeId.isEmpty())
            throw RepositoryException.EmptyIdException("")

        val currentUser = currentUser()
            ?: throw ChallengeRepository.ChallengeException.RequiredCurrentUserException("")

        return flow {
            emit(recordRemoteSource.deleteRecord(challengeId, currentUser.uid))
        }.flowOn(
            Dispatchers.IO
        ).first()
    }
}