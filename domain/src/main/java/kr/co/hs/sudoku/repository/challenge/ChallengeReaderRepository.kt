package kr.co.hs.sudoku.repository.challenge

import kr.co.hs.sudoku.model.challenge.ChallengeEntity

interface ChallengeReaderRepository {
    suspend fun getChallengeDetail(challengeId: String): ChallengeEntity
    suspend fun getChallengeList(count: Long): List<ChallengeEntity>
    fun clearCache()
}