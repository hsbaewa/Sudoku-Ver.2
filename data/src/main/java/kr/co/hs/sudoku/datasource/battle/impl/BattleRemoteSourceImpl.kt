package kr.co.hs.sudoku.datasource.battle.impl

import com.google.firebase.Timestamp
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions.merge
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.FireStoreRemoteSource
import kr.co.hs.sudoku.datasource.battle.BattleRemoteSource
import kr.co.hs.sudoku.mapper.Mapper.asMutableMap
import kr.co.hs.sudoku.model.battle.BattleLeaderBoardModel
import kr.co.hs.sudoku.model.battle.BattleModel
import kr.co.hs.sudoku.model.battle.BattleParticipantModel
import kr.co.hs.sudoku.model.battle.BattleStatisticsModel
import kr.co.hs.sudoku.model.record.ClearTimeRecordModel
import java.util.Date

class BattleRemoteSourceImpl : FireStoreRemoteSource(), BattleRemoteSource {
    override fun getBattleCollectionRef() = rootDocument.collection("battle")
    override fun getParticipantCollectionRef() = rootDocument.collection("battleParticipants")

    override fun getBattleRecordCollectionRef(battleId: String) =
        getBattleCollectionRef()
            .document(battleId)
            .collection("record")

    override fun getBattleLeaderBoardCollectionRef() = rootDocument.collection("battleLeaderBoard")

    override fun createBattle(transaction: Transaction, battleModel: BattleModel): String {
        val documentRef = getBattleCollectionRef().document()
        transaction.set(documentRef, battleModel.toFirebaseData())
        return documentRef.id
    }

    override fun getBattle(transaction: Transaction, battleId: String) =
        transaction
            .get(getBattleCollectionRef().document(battleId))
            .toBattleModel()

    private fun DocumentSnapshot.toBattleModel() =
        toObject(BattleModel::class.java)
            ?.apply { this.id = this@toBattleModel.id }

    fun changeToBattleModel(snapshot: DocumentSnapshot?) = snapshot?.toBattleModel()

    override fun getParticipant(transaction: Transaction, uid: String) =
        transaction
            .get(getParticipantCollectionRef().document(uid))
            .toParticipantModel()

    private fun DocumentSnapshot.toParticipantModel() = toObject(BattleParticipantModel::class.java)

    fun changeToParticipantModel(snapshot: DocumentSnapshot?) = snapshot?.toParticipantModel()

    override fun getBattleRecord(transaction: Transaction, battleId: String, uid: String) =
        transaction
            .get(getBattleRecordCollectionRef(battleId).document(uid))
            .toObject(ClearTimeRecordModel::class.java)

    override suspend fun getBattleRecord(battleId: String, uid: String) =
        getBattleRecordCollectionRef(battleId)
            .document(uid)
            .get()
            .await()
            .toObject(ClearTimeRecordModel::class.java)

    override suspend fun getBattle(battleId: String) =
        getBattleCollectionRef()
            .document(battleId)
            .get()
            .await()
            .toBattleModel()

    override suspend fun getBattleList(limit: Long, firstCreateTime: Long) =
        getBattleCollectionRef()
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .whereEqualTo("pendingAt", null)
            .let {
                firstCreateTime
                    .takeIf { first -> first >= 0 }
                    ?.run { it.startAfter(Timestamp(Date(this))) }
                    ?: it
            }
            .get()
            .await()
            .documents
            .mapNotNull { it.toBattleModel() }

    override suspend fun getBattleListCreatedBy(uid: String) =
        getBattleCollectionRef()
            .orderBy("hostUid")
            .startAt(uid)
            .endAt(uid + "\uf8ff")
            .get()
            .await()
            .documents
            .mapNotNull { it.toBattleModel() }

    override suspend fun getParticipantList(battleId: String) =
        getParticipantCollectionRef()
            .whereEqualTo("battleId", battleId)
            .get()
            .await()
            .documents
            .mapNotNull { it.toParticipantModel() }

    override suspend fun getParticipant(uid: String) =
        getParticipantCollectionRef()
            .document(uid)
            .get()
            .await()
            .toParticipantModel()

    override suspend fun getStatistics(uid: String) =
        BattleStatisticsModel(uid, getWinCount(uid), getPlayCount(uid))

    private suspend fun getWinCount(uid: String) =
        getBattleCollectionRef()
            .whereEqualTo("winnerUid", uid)
            .count()
            .get(AggregateSource.SERVER)
            .await()
            .count

    private suspend fun getPlayCount(uid: String) =
        getBattleCollectionRef()
            .whereArrayContains("startingParticipants", uid)
            .count()
            .get(AggregateSource.SERVER)
            .await()
            .count

    override suspend fun updateBattle(battleId: String, data: Map<String, Any?>) {
        getBattleCollectionRef().document(battleId).set(data, merge())
    }

    override fun updateBattle(transaction: Transaction, battleId: String, data: Map<String, Any?>) {
        transaction.set(getBattleCollectionRef().document(battleId), data, merge())
    }

    override fun setBattleRecord(
        transaction: Transaction,
        battleId: String,
        uid: String,
        data: Map<String, Any?>
    ) {
        transaction.set(
            getBattleRecordCollectionRef(battleId).document(uid),
            data,
            merge()
        )
    }

    override fun setParticipant(
        transaction: Transaction,
        battleParticipantModel: BattleParticipantModel
    ) {
        transaction.set(
            getParticipantCollectionRef().document(battleParticipantModel.uid),
            battleParticipantModel.asMutableMap()
        )
    }

    override fun setParticipant(transaction: Transaction, uid: String, data: Map<String, Any?>) {
        transaction.update(getParticipantCollectionRef().document(uid), data)
    }

    override suspend fun setParticipant(uid: String, data: Map<String, Any?>) {
        getParticipantCollectionRef().document(uid).set(data, merge())
    }

    override fun deleteBattle(transaction: Transaction, battleId: String) {
        transaction.delete(getBattleCollectionRef().document(battleId))
    }

    override fun deleteParticipant(transaction: Transaction, participant: BattleParticipantModel) {
        transaction.delete(getParticipantCollectionRef().document(participant.uid))
    }

    override fun deleteParticipant(transaction: Transaction, uid: String) {
        transaction.delete(getParticipantCollectionRef().document(uid))
    }

    override suspend fun registerLeaderBoard(uid: String) {
        getBattleLeaderBoardCollectionRef()
            .document(uid)
            .set(BattleStatisticsModel(uid, getWinCount(uid), getPlayCount(uid)), merge())
            .await()
    }

    override suspend fun getLeaderBoard(limit: Long) = getBattleLeaderBoardCollectionRef()
        .orderBy("winCount", Query.Direction.DESCENDING)
        .orderBy("playCount", Query.Direction.ASCENDING)
        .limit(limit)
        .get()
        .await()
        .documents.mapNotNull { it.toObject(BattleLeaderBoardModel::class.java) }

    override suspend fun getLeaderBoardModel(uid: String): BattleLeaderBoardModel {
        val model = getBattleLeaderBoardCollectionRef()
            .document(uid)
            .get()
            .await()
            .toObject(BattleLeaderBoardModel::class.java)
            ?: throw Exception("not found")

        var cnt = getBattleLeaderBoardCollectionRef()
            .orderBy("winCount", Query.Direction.DESCENDING)
            .whereGreaterThan("winCount", model.winCount)
            .count()
            .get(AggregateSource.SERVER)
            .await()
            .count


        cnt += getBattleLeaderBoardCollectionRef()
            .whereEqualTo("winCount", model.winCount)
            .get()
            .await()
            .map { it.toObject(BattleStatisticsModel::class.java) }
            .count { it.playCount <= model.playCount }

        model.ranking = cnt
        return model
    }
}