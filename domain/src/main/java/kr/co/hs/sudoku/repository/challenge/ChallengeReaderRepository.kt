package kr.co.hs.sudoku.repository.challenge

import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import java.util.Date

interface ChallengeReaderRepository {
    suspend fun getChallenge(challengeId: String): ChallengeEntity
    suspend fun getLatestChallenge(): ChallengeEntity
    suspend fun getChallengeIds(): List<String>
    suspend fun getChallenge(createdAt: Date): ChallengeEntity
    suspend fun getChallenges(startAt: Date): List<ChallengeEntity>
    suspend fun getChallenges(count: Long): List<ChallengeEntity>
    fun clearCache()
}