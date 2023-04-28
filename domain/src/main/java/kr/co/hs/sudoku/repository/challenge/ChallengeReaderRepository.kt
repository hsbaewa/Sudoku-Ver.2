package kr.co.hs.sudoku.repository.challenge

import kr.co.hs.sudoku.model.challenge.ChallengeEntity

interface ChallengeReaderRepository {
    suspend fun getChallenge(challengeId: String): ChallengeEntity
    suspend fun getLatestChallenge(): ChallengeEntity
    suspend fun getChallengeIds(): List<String>
}