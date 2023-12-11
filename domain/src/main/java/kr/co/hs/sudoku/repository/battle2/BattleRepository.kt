package kr.co.hs.sudoku.repository.battle2

import kr.co.hs.sudoku.model.battle2.BattleEntity
import kr.co.hs.sudoku.model.battle.BattleStatisticsEntity
import kr.co.hs.sudoku.model.matrix.IntMatrix

interface BattleRepository {
    // 현재의 user id
    val currentUserUid: String

    /**
     * 방 생성
     */
    suspend fun create(matrix: IntMatrix): BattleEntity
    suspend fun create(matrix: IntMatrix, participantSize: Int): BattleEntity


    /**
     * 방 찾기
     */
    suspend fun search(battleId: String): BattleEntity
    suspend fun list(): List<BattleEntity>

    /**
     * 방 참여자 정보 획득
     */
    suspend fun getParticipants(battleEntity: BattleEntity)
    suspend fun searchWithParticipants(battleId: String): BattleEntity


    /**
     * 내가 참여중인 방 확인
     */
    suspend fun isParticipating(): Boolean
    suspend fun getParticipating(): BattleEntity


    /**
     * 방 참여
     */
    suspend fun join(battleId: String)


    /**
     * 준비
     */
    suspend fun ready()
    suspend fun unready()

    /**
     * 시작
     */
    suspend fun pendingStart()
    suspend fun start()

    /**
     * 조작
     */
    suspend fun updateMatrix(row: Int, column: Int, value: Int)

    /**
     * 기록
     */
    suspend fun clear(record: Long)


    /**
     * 나가기
     */
    suspend fun exit()


    /**
     * 기록 조회
     */
    suspend fun getStatistics(): BattleStatisticsEntity


}