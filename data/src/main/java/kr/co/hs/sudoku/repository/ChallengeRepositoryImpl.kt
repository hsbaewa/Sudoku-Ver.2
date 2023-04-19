package kr.co.hs.sudoku.repository

import kr.co.hs.sudoku.datasource.challenge.ChallengeRemoteSourceImpl
import kr.co.hs.sudoku.mapper.ChallengeMapper.toDomain
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.challenge.ChallengeModel
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository

class ChallengeRepositoryImpl : ChallengeRepository {

    override suspend fun createChallenge(entity: ChallengeEntity) =
        remoteSource.createChallenge(entity.toData())

    private val remoteSource = ChallengeRemoteSourceImpl()

    private fun ChallengeEntity.toData() = ChallengeModel(
        boxSize = matrix.boxSize,
        boxCount = matrix.boxCount,
        rowCount = matrix.rowCount,
        columnCount = matrix.columnCount,
        matrix = matrix.flatten()
    ).apply { id = challengeId }

    override suspend fun removeChallenge(entity: ChallengeEntity) =
        entity.challengeId
            .takeIf { it != null }
            ?.run { remoteSource.removeChallenge(this) }
            ?: false

    override suspend fun getChallenge(challengeId: String) =
        remoteSource.getChallenge(challengeId).toDomain()

    override suspend fun getLatestChallenge() =
        remoteSource.getLatestChallenge().toDomain()

    override suspend fun getChallengeIds() = remoteSource.getChallengeIds()
}