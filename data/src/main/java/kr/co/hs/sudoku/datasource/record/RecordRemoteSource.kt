package kr.co.hs.sudoku.datasource.record

import kr.co.hs.sudoku.model.record.ClearTimeRecordModel

interface RecordRemoteSource {
    suspend fun getRecords(challengeId: String, limit: Int): List<ClearTimeRecordModel>
    suspend fun addRecord(challengeId: String, record: ClearTimeRecordModel): Boolean
    suspend fun getRecord(challengeId: String, uid: String): ClearTimeRecordModel
}