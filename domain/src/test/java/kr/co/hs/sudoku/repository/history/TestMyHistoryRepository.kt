package kr.co.hs.sudoku.repository.history

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kr.co.hs.sudoku.data.TestHistoryDataSource
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.history.HistoryEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.RepositoryException
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class TestMyHistoryRepository @Inject constructor(
    private val dataSource: TestHistoryDataSource,
    private val getCurrentUserProfile: () -> Flow<ProfileEntity>
) : MyHistoryRepository {
    override suspend fun createChallengeClearHistory(
        challenge: ChallengeEntity,
        record: Long
    ): HistoryEntity {
        val currentUser = getCurrentUserProfile().firstOrNull()
            ?: throw MyHistoryRepository.MyHistoryException.RequiredCurrentUserException("")

        val entity = HistoryEntity.ChallengeClear(
            Random.nextInt().toString(),
            Date(),
            currentUser.uid,
            challengeId = challenge.challengeId,
            -1L,
            Date(),
            record
        )
        dataSource.dummyData[entity.id] = entity

        return entity
    }

    override suspend fun createBattleClearHistory(
        battle: BattleEntity,
        record: Long
    ): HistoryEntity {
        val currentUser = getCurrentUserProfile().firstOrNull()
            ?: throw MyHistoryRepository.MyHistoryException.RequiredCurrentUserException("")

        val entity = HistoryEntity.BattleClear(
            Random.nextInt().toString(),
            Date(),
            currentUser.uid,
            battle.participants.map { it.uid },
            record,
            battle.id
        )

        dataSource.dummyData[entity.id] = entity

        return entity
    }

    override suspend fun getList(createdAt: Date, count: Long): List<HistoryEntity> {
        return dataSource.dummyData.values
            .sortedByDescending { it.createdAt }
            .filter { it.createdAt.time < createdAt.time }
            .take(count.toInt())
    }

    override suspend fun delete(id: String) {
        dataSource.dummyData.remove(id)
    }

    override suspend fun get(id: String): HistoryEntity {
        return dataSource.dummyData[id]
            ?: throw RepositoryException.NotFoundException("entity not found")
    }
}