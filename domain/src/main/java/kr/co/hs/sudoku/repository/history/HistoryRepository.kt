package kr.co.hs.sudoku.repository.history

import kr.co.hs.sudoku.model.history.HistoryEntity
import kr.co.hs.sudoku.repository.RepositoryException
import java.util.Date

interface HistoryRepository {
    @Throws(RepositoryException::class)
    suspend fun getList(uid: String, createdAt: Date, count: Long): List<HistoryEntity>

    @Throws(RepositoryException::class)
    suspend fun get(id: String): HistoryEntity
}