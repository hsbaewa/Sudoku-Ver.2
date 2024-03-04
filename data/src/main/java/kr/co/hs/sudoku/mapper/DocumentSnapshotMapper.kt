package kr.co.hs.sudoku.mapper

import com.google.firebase.firestore.DocumentSnapshot
import kr.co.hs.sudoku.model.battle.BattleModel
import kr.co.hs.sudoku.model.battle.BattleParticipantModel

object DocumentSnapshotMapper {
    fun DocumentSnapshot.toBattleModel() = toObject(BattleModel::class.java)
        ?.apply { this.id = this@toBattleModel.id }

    fun DocumentSnapshot.toParticipantModel() = toObject(BattleParticipantModel::class.java)
}