package kr.co.hs.sudoku.repository.battle

import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.battle.BattleParticipantEntity
import kr.co.hs.sudoku.model.battle.BattleStatisticsEntity
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.user.ProfileEntity
import java.util.Date

interface BattleRepository {
    suspend fun createBattle(profile: ProfileEntity, matrix: IntMatrix): BattleEntity

    suspend fun createBattle(
        profile: ProfileEntity,
        matrix: IntMatrix,
        participantSize: Int
    ): BattleEntity

    class BattleCreateFailedException(p0: String? = null) : Exception(p0)

    suspend fun getBattle(battleId: String): BattleEntity?
    suspend fun getBattleList(limit: Long): List<BattleEntity>
    suspend fun getBattleList(limit: Long, lastAt: Date): List<BattleEntity>
    suspend fun getBattleListCreatedBy(uid: String): List<BattleEntity>
    suspend fun getParticipantList(battleId: String): List<BattleParticipantEntity>
    suspend fun getParticipant(uid: String): BattleParticipantEntity?

    suspend fun joinBattle(battleEntity: BattleEntity, profile: ProfileEntity): BattleEntity?
    suspend fun joinBattle(battleId: String, profile: ProfileEntity): BattleEntity?
    suspend fun getJoinedBattle(uid: String): BattleEntity?

    class UnknownBattleException(p0: String? = null) : Exception(p0)

    suspend fun readyToBattle(uid: String)
    suspend fun unreadyToBattle(uid: String)
    suspend fun isAllReady(battleEntity: BattleEntity): Boolean

    suspend fun exitBattle(battleEntity: BattleEntity, profile: ProfileEntity)
    suspend fun exitBattle(battleEntity: BattleEntity, uid: String)

    suspend fun startBattle(battleEntity: BattleEntity, uid: String)
    suspend fun startBattle(battleEntity: BattleEntity)

    suspend fun updateClearRecord(
        battleEntity: BattleEntity,
        profile: ProfileEntity,
        clearTime: Long
    )

    class UnknownParticipantException(p0: String? = null) : Exception(p0)

    suspend fun getStatistics(uid: String): BattleStatisticsEntity

    fun bindBattle(battleId: String, changedListener: BattleChangedListener)
    fun unbindBattle(battleId: String)
    interface BattleChangedListener {
        fun onChanged(battle: BattleEntity)
    }

    fun bindParticipant(battleId: String, uid: String, changedListener: ParticipantChangedListener)
    fun unbindParticipant(battleId: String, uid: String)
    interface ParticipantChangedListener {
        fun onChanged(participant: BattleParticipantEntity)
    }

    suspend fun updateParticipantMatrix(uid: String, matrix: List<List<Int>>)

    suspend fun pendingBattle(battleEntity: BattleEntity, uid: String)
}