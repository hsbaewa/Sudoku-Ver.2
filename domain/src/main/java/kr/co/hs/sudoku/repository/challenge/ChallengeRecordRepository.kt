package kr.co.hs.sudoku.repository.challenge

import kr.co.hs.sudoku.repository.record.RecordRepository

interface ChallengeRecordRepository : RecordRepository {
    fun setChallengeId(id: String)
}