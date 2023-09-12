package kr.co.hs.sudoku.repository.record

import kr.co.hs.sudoku.model.rank.RankerEntity

interface RecordReaderRepository {
    suspend fun getRecords(limit: Int): List<RankerEntity>
    suspend fun getRecord(uid: String): RankerEntity
}