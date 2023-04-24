package kr.co.hs.sudoku.repository.challenge

import kr.co.hs.sudoku.datasource.challenge.ChallengeRemoteSource
import kr.co.hs.sudoku.datasource.challenge.impl.ChallengeRemoteSourceImpl
import kr.co.hs.sudoku.mapper.ChallengeMapper.toDomain

class ChallengeReaderRepositoryImpl(
    private val remoteSource: ChallengeRemoteSource = ChallengeRemoteSourceImpl()
) : ChallengeReaderRepository {
    override suspend fun getChallenge(challengeId: String) =
        remoteSource.getChallenge(challengeId).toDomain()

    override suspend fun getLatestChallenge() =
        remoteSource.getLatestChallenge().toDomain()

    override suspend fun getChallengeIds() = remoteSource.getChallengeIds()
}