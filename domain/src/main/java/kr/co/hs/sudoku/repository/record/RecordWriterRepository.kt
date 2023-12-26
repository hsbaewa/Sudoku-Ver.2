package kr.co.hs.sudoku.repository.record

import kr.co.hs.sudoku.model.rank.RankerEntity

interface RecordWriterRepository {
    suspend fun putRecord(entity: RankerEntity): Boolean
    suspend fun putRecord(clearRecord: Long): Boolean
}