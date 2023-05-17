package kr.co.hs.sudoku.datasource.battle

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Transaction
import kr.co.hs.sudoku.model.battle.BattleModel
import kr.co.hs.sudoku.model.battle.BattleParticipantModel
import kr.co.hs.sudoku.model.battle.BattleStatisticsModel
import kr.co.hs.sudoku.model.record.ClearTimeRecordModel

interface BattleRemoteSource {
    fun getBattleCollectionRef(): CollectionReference
    fun getParticipantCollectionRef(): CollectionReference
    fun getBattleRecordCollectionRef(battleId: String): CollectionReference

    fun createBattle(transaction: Transaction, battleModel: BattleModel): String

    fun getBattle(transaction: Transaction, battleId: String): BattleModel?
    fun getParticipant(transaction: Transaction, uid: String): BattleParticipantModel?
    fun getBattleRecord(
        transaction: Transaction,
        battleId: String,
        uid: String
    ): ClearTimeRecordModel?

    suspend fun getBattle(battleId: String): BattleModel?
    suspend fun getBattleList(limit: Long, firstCreateTime: Long): List<BattleModel>
    suspend fun getBattleListCreatedBy(uid: String): List<BattleModel>
    suspend fun getParticipantList(battleId: String): List<BattleParticipantModel>
    suspend fun getParticipant(uid: String): BattleParticipantModel?

    suspend fun getStatistics(uid: String): BattleStatisticsModel

    fun updateBattle(transaction: Transaction, battleId: String, data: Map<String, Any?>)
    fun setBattleRecord(
        transaction: Transaction,
        battleId: String,
        uid: String,
        data: Map<String, Any?>
    )

    fun setParticipant(transaction: Transaction, battleParticipantModel: BattleParticipantModel)
    fun setParticipant(transaction: Transaction, uid: String, data: Map<String, Any?>)

    fun deleteBattle(transaction: Transaction, battleId: String)
    fun deleteParticipant(transaction: Transaction, participant: BattleParticipantModel)
    fun deleteParticipant(transaction: Transaction, uid: String)
}