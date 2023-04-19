package kr.co.hs.sudoku.datasource.rank

import kr.co.hs.sudoku.model.record.ClearTimeRecordModel

interface RankingRemoteSource {
    suspend fun getRecords(): List<ClearTimeRecordModel>
    suspend fun addRecord(record: ClearTimeRecordModel): Boolean
    suspend fun getRecord(uid: String): ClearTimeRecordModel
}