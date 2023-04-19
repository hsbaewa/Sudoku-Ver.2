package kr.co.hs.sudoku.repository.rank

import kr.co.hs.sudoku.model.rank.RankerEntity

interface RankingRepository {
    suspend fun getRanking(): List<RankerEntity>
    suspend fun putRecord(entity: RankerEntity): Boolean
    suspend fun getRecord(uid: String): RankerEntity
}