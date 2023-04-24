package kr.co.hs.sudoku.repository.record

import kr.co.hs.sudoku.model.rank.RankerEntity

interface RecordRepository {
    suspend fun getRecords(limit: Int): List<RankerEntity>
    suspend fun putRecord(entity: RankerEntity): Boolean
    suspend fun getRecord(uid: String): RankerEntity
}