package kr.co.hs.sudoku.datasource.challenge

import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.challenge.ClearTimeRecordModel
import kr.co.hs.sudoku.model.challenge.ReserveRecordModel

interface ChallengeRecordRemoteSource {
    suspend fun getRecords(id: String, limit: Int): List<ClearTimeRecordModel>
    suspend fun setRecord(id: String, record: ClearTimeRecordModel): Boolean
    suspend fun setRecord(id: String, record: ReserveRecordModel): Boolean
    suspend fun getRecord(id: String, uid: String): ClearTimeRecordModel
    suspend fun getReservedMyRecord(id: String, uid: String): ReserveRecordModel
    suspend fun deleteRecord(id: String, uid: String): Boolean
    suspend fun getChallengeMetadata(challengeEntity: ChallengeEntity, uid: String): Boolean
}