package kr.co.hs.sudoku.repository.history

import kr.co.hs.sudoku.data.TestHistoryDataSource
import kr.co.hs.sudoku.model.history.HistoryEntity
import kr.co.hs.sudoku.repository.RepositoryException
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestHistoryRepository
@Inject constructor(
    private val dataSource: TestHistoryDataSource,
) : HistoryRepository {

    override suspend fun getList(uid: String, createdAt: Date, count: Long): List<HistoryEntity> {
        return dataSource.dummyData.values
            .sortedByDescending { it.createdAt }
            .filter { it.createdAt.time < createdAt.time }
            .filter { it.uid == uid }
            .take(count.toInt())
    }


    override suspend fun get(id: String): HistoryEntity {
        return dataSource.dummyData[id]
            ?: throw RepositoryException.NotFoundException("entity not found")
    }
}