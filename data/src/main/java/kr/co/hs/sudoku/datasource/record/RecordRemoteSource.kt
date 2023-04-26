package kr.co.hs.sudoku.datasource.record

import kr.co.hs.sudoku.model.record.ClearTimeRecordModel
import kr.co.hs.sudoku.model.record.ReserveRecordModel

interface RecordRemoteSource {
    suspend fun getRecords(challengeId: String, limit: Int): List<ClearTimeRecordModel>
    suspend fun setRecord(challengeId: String, record: ClearTimeRecordModel): Boolean
    suspend fun setRecord(challengeId: String, record: ReserveRecordModel): Boolean
    suspend fun getRecord(challengeId: String, uid: String): ClearTimeRecordModel
    suspend fun getReservedMyRecord(challengeId: String, uid: String): ReserveRecordModel
}