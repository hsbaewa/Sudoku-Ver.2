package kr.co.hs.sudoku.repository.challenge

import kotlinx.coroutines.coroutineScope
import kr.co.hs.sudoku.datasource.challenge.ChallengeRecordRemoteSource
import kr.co.hs.sudoku.datasource.challenge.ChallengeRemoteSource
import kr.co.hs.sudoku.datasource.logs.LogRemoteSource
import kr.co.hs.sudoku.mapper.ChallengeMapper.toDomain
import kr.co.hs.sudoku.mapper.RecordMapper.toDomain
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.challenge.ChallengeModel
import kr.co.hs.sudoku.model.logs.ChallengeClearLogEntity
import kr.co.hs.sudoku.model.logs.LogModel
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.model.challenge.ClearTimeRecordModel
import kr.co.hs.sudoku.model.challenge.ReserveRecordModel
import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.LocaleModel
import kr.co.hs.sudoku.usecase.user.GetCurrentUserProfileUseCase
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ChallengeRepositoryImpl
@Inject constructor(
    private val challengeRemoteSource: ChallengeRemoteSource,
    private val recordRemoteSource: ChallengeRecordRemoteSource,
    private val logRemoteSource: LogRemoteSource,
    private val getCurrentUserProfileUseCase: GetCurrentUserProfileUseCase
) : ChallengeRepository {

    override suspend fun getChallenges(createdAt: Date, count: Long): List<ChallengeEntity> {
        val currentUserProfile = coroutineScope { getCurrentUserProfileUseCase(this) }
        return challengeRemoteSource.getChallenges(createdAt, count)
            .mapNotNull { it.toDomain() }
            .onEach { recordRemoteSource.getChallengeMetadata(it, currentUserProfile.uid) }
    }

    override suspend fun getChallenge(id: String): ChallengeEntity {
        val currentUserProfile = coroutineScope { getCurrentUserProfileUseCase(this) }
        return challengeRemoteSource.getChallenge(id).toDomain()
            ?.also { recordRemoteSource.getChallengeMetadata(it, currentUserProfile.uid) }
            ?: throw Exception("unknown challenge")
    }

    override suspend fun createChallenge(entity: ChallengeEntity) =
        entity.createdAt.takeIf { it != null }
            ?.run { challengeRemoteSource.createChallenge(entity.toData(), this) }
            ?: run { challengeRemoteSource.createChallenge(entity.toData()) }

    private fun ChallengeEntity.toData() = ChallengeModel(
        boxSize = matrix.boxSize,
        boxCount = matrix.boxCount,
        rowCount = matrix.rowCount,
        columnCount = matrix.columnCount,
        matrix = matrix.flatten()
    ).apply { id = challengeId }

    override suspend fun removeChallenge(challengeId: String): Boolean {
        return challengeRemoteSource.removeChallenge(challengeId)
    }

    override suspend fun getRecords(challengeId: String): List<RankerEntity> {
        return recordRemoteSource.getRecords(challengeId, 10).map { it.toDomain() }
    }

    override suspend fun getRecord(challengeId: String, uid: String): RankerEntity {
        return recordRemoteSource.getRecord(challengeId, uid).toDomain()
    }

    override suspend fun putRecord(challengeId: String, clearRecord: Long): Boolean {
        val currentUserProfile = coroutineScope { getCurrentUserProfileUseCase(this) }
        return recordRemoteSource.setRecord(
            challengeId,
            RankerEntity(currentUserProfile, clearRecord).toData()
        )
    }

    override suspend fun putReserveRecord(challengeId: String): Boolean {
        val currentUserProfile = coroutineScope { getCurrentUserProfileUseCase(this) }
        return recordRemoteSource.setRecord(
            challengeId,
            ReserveRecordModel(currentUserProfile.uid, "", null, null, null)
        )
    }

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

    override suspend fun deleteRecord(challengeId: String, uid: String): Boolean {
        return recordRemoteSource.deleteRecord(challengeId, uid)
    }

    override suspend fun getHistory(
        uid: String,
        createdAt: Date,
        count: Long
    ): List<ChallengeClearLogEntity> =
        logRemoteSource
            .getLogs(LogModel.ChallengeClear::class.java, uid, createdAt, count)
            .mapNotNull {
                val challengeId = it.challengeId
                val challenge = runCatching { getChallenge(challengeId) }.getOrNull()
                    ?: return@mapNotNull null
                val challengeCreatedAt = challenge.createdAt ?: return@mapNotNull null
                val grade = runCatching { getRecord(challengeId, uid).rank }.getOrNull()
                    ?: return@mapNotNull null
                object : ChallengeClearLogEntity {
                    override val id: String = it.id
                    override val challengeId: String = it.challengeId
                    override val grade: Long = grade
                    override val clearAt: Date = it.createdAt.toDate()
                    override val uid: String = it.uid
                    override val record: Long = it.record
                    override val createdAt: Date = challengeCreatedAt
                }
            }

    override suspend fun getHistory(uid: String, count: Long) =
        getHistory(uid, Date(System.currentTimeMillis() + 60 * 1000), count)
}