package kr.co.hs.sudoku.repository.challenge

import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.repository.RepositoryException
import java.util.Date

interface ChallengeRepository {
    @Throws(RepositoryException::class, ChallengeException::class)
    suspend fun getChallenges(createdAt: Date, count: Long): List<ChallengeEntity>

    @Throws(RepositoryException::class, ChallengeException::class)
    suspend fun getChallenge(id: String): ChallengeEntity

    @Throws(RepositoryException::class, ChallengeException::class)
    suspend fun createChallenge(entity: ChallengeEntity): ChallengeEntity

    @Throws(RepositoryException::class, ChallengeException::class)
    suspend fun removeChallenge(challengeId: String): Boolean

    @Throws(RepositoryException::class, ChallengeException::class)
    suspend fun getRecords(challengeId: String): List<RankerEntity>

    @Throws(RepositoryException::class, ChallengeException::class)
    suspend fun getRecord(challengeId: String): RankerEntity

    @Throws(RepositoryException::class, ChallengeException::class)
    suspend fun putRecord(challengeId: String, clearRecord: Long): Boolean

    @Throws(RepositoryException::class, ChallengeException::class)
    suspend fun putReserveRecord(challengeId: String): Boolean

    @Throws(RepositoryException::class, ChallengeException::class)
    suspend fun deleteRecord(challengeId: String): Boolean

    sealed class ChallengeException(p0: String?) : Exception(p0) {
        class RequiredCurrentUserException(p0: String?) : ChallengeException(p0)
    }
}