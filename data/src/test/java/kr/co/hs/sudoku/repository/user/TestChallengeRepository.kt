package kr.co.hs.sudoku.repository.user

import kr.co.hs.sudoku.datasource.challenge.ChallengeRecordRemoteSource
import kr.co.hs.sudoku.datasource.challenge.ChallengeRemoteSource
import kr.co.hs.sudoku.datasource.logs.LogRemoteSource
import kr.co.hs.sudoku.mapper.ChallengeMapper.toDomain
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.challenge.ClearTimeRecordModel
import kr.co.hs.sudoku.model.challenge.ReserveRecordModel
import kr.co.hs.sudoku.model.logs.ChallengeClearLogEntity
import kr.co.hs.sudoku.model.logs.LogModel
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.repository.challenge.ChallengeRepositoryImpl
import kr.co.hs.sudoku.usecase.user.GetCurrentUserProfileUseCase
import java.util.Date
import javax.inject.Inject

class TestChallengeRepository
@Inject constructor(
    private val challengeRemoteSource: ChallengeRemoteSource,
    private val recordRemoteSource: ChallengeRecordRemoteSource,
    private val logRemoteSource: LogRemoteSource,
    getCurrentUserProfileUseCase: GetCurrentUserProfileUseCase,
    private val uid: String
) : ChallengeRepository by ChallengeRepositoryImpl(
    challengeRemoteSource,
    recordRemoteSource,
    logRemoteSource,
    getCurrentUserProfileUseCase
) {
    override suspend fun getChallenges(createdAt: Date, count: Long): List<ChallengeEntity> {
        return challengeRemoteSource.getChallenges(createdAt, count)
            .mapNotNull { it.toDomain() }
            .onEach { recordRemoteSource.getChallengeMetadata(it, uid) }
    }

    override suspend fun getChallenge(id: String): ChallengeEntity {
        return challengeRemoteSource.getChallenge(id).toDomain()
            ?.also { recordRemoteSource.getChallengeMetadata(it, uid) }
            ?: throw Exception("unknown challenge")
    }

    override suspend fun putRecord(challengeId: String, clearRecord: Long): Boolean {
        val model = ClearTimeRecordModel(
            uid = uid,
            name = "name",
            message = "message",
            iconUrl = "",
            locale = null,
            clearTime = clearRecord
        )
        return recordRemoteSource.setRecord(
            challengeId,
            model
        )
    }

    override suspend fun putReserveRecord(challengeId: String): Boolean {
        return recordRemoteSource.setRecord(
            challengeId,
            ReserveRecordModel(uid, "", null, null, null)
        )
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