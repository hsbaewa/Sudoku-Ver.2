package kr.co.hs.sudoku.datasource.record

import kr.co.hs.sudoku.model.record.ClearTimeRecordModel
import kr.co.hs.sudoku.model.record.ReserveRecordModel

interface RecordRemoteSource {
    suspend fun getRecords(id: String, limit: Int): List<ClearTimeRecordModel>
    suspend fun setRecord(id: String, record: ClearTimeRecordModel): Boolean
    suspend fun setRecord(id: String, record: ReserveRecordModel): Boolean
    suspend fun getRecord(id: String, uid: String): ClearTimeRecordModel
    suspend fun getReservedMyRecord(id: String, uid: String): ReserveRecordModel
    suspend fun deleteRecord(id: String, uid: String): Boolean
}