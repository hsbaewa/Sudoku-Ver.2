package kr.co.hs.sudoku.repository.challenge

import kr.co.hs.sudoku.data.TestChallengeDataSource
import kr.co.hs.sudoku.data.TestRecordDataSource
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.repository.RepositoryException
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestChallengeRepository
@Inject constructor(
    private val dataSource: TestChallengeDataSource,
    private val recordDataSource: TestRecordDataSource
) : ChallengeRepository {
    override suspend fun getChallenges(createdAt: Date, count: Long): List<ChallengeEntity> {
        return dataSource.dummyData.values
            .filter {
                val itemCreatedAt = it.createdAt ?: return@filter false
                itemCreatedAt < createdAt
            }
            .sortedByDescending { it.createdAt }
            .take(count.toInt())
    }

    override suspend fun getChallenge(id: String): ChallengeEntity {
        if (id.isEmpty())
            throw RepositoryException.EmptyIdException("")

        return dataSource.dummyData.getOrDefault(id, null)
            ?: throw RepositoryException.NotFoundException("")
    }

    override suspend fun createChallenge(entity: ChallengeEntity): ChallengeEntity {
        dataSource.dummyData[entity.challengeId] = entity
        return entity
    }

    override suspend fun removeChallenge(challengeId: String): Boolean {
        return dataSource.dummyData.takeIf { it.containsKey(challengeId) }
            ?.let {
                it.remove(challengeId)
                true
            } ?: false
    }

    override suspend fun getRecords(challengeId: String): List<RankerEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun getRecord(challengeId: String): RankerEntity {
        TODO("Not yet implemented")
    }

    override suspend fun putRecord(challengeId: String, clearRecord: Long): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun putReserveRecord(challengeId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteRecord(challengeId: String): Boolean {
        TODO("Not yet implemented")
    }

}