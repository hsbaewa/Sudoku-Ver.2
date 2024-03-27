package kr.co.hs.sudoku.repository.history

import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.history.HistoryEntity
import kr.co.hs.sudoku.repository.RepositoryException
import java.util.Date

interface MyHistoryRepository {
    @Throws(RepositoryException::class, MyHistoryException::class)
    suspend fun createChallengeClearHistory(challenge: ChallengeEntity, record: Long): HistoryEntity

    @Throws(RepositoryException::class, MyHistoryException::class)
    suspend fun createBattleClearHistory(battle: BattleEntity, record: Long): HistoryEntity

    @Throws(RepositoryException::class, MyHistoryException::class)
    suspend fun getList(createdAt: Date, count: Long): List<HistoryEntity>

    @Throws(RepositoryException::class, MyHistoryException::class)
    suspend fun delete(id: String)

    @Throws(RepositoryException::class, MyHistoryException::class)
    suspend fun get(id: String): HistoryEntity

    sealed class MyHistoryException(p0: String?) : Exception(p0) {
        class RequiredCurrentUserException(p0: String?) : MyHistoryException(p0)
        class NotMineException(p0: String?) : MyHistoryException(p0)
    }
}