package kr.co.hs.sudoku.repository.challenge

import kr.co.hs.sudoku.datasource.challenge.ChallengeRemoteSource
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.challenge.ChallengeModel

class ChallengeWriterRepositoryImpl(
    private val remoteSource: ChallengeRemoteSource
) : ChallengeWriterRepository {
    override suspend fun createChallenge(entity: ChallengeEntity) =
        remoteSource.createChallenge(entity.toData())

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

}