package kr.co.hs.sudoku.repository.challenge

import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.rank.RankerEntity
import java.util.Date

interface ChallengeRepository {
    suspend fun getChallenges(createdAt: Date, count: Long): List<ChallengeEntity>
    suspend fun getChallenge(id: String): ChallengeEntity
    suspend fun createChallenge(entity: ChallengeEntity): Boolean
    suspend fun removeChallenge(challengeId: String): Boolean
    suspend fun getRecords(challengeId: String): List<RankerEntity>
    suspend fun getRecord(challengeId: String, uid: String): RankerEntity
    suspend fun putRecord(challengeId: String, clearRecord: Long): Boolean
    suspend fun putReserveRecord(challengeId: String): Boolean
    suspend fun deleteRecord(challengeId: String, uid: String): Boolean
    fun clearCache()
}