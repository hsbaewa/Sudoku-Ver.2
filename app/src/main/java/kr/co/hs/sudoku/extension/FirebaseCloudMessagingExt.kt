package kr.co.hs.sudoku.extension

import com.google.firebase.messaging.FirebaseMessaging
import kr.co.hs.sudoku.model.battle.BattleEntity

object FirebaseCloudMessagingExt {
    fun FirebaseMessaging.subscribeAllUser() =
        subscribeToTopic("sudoku.user.all")

    fun FirebaseMessaging.subscribeUser(uid: String) =
        subscribeToTopic("sudoku.user.$uid")

    fun FirebaseMessaging.subscribeBattle(battleEntity: BattleEntity) =
        subscribeToTopic("sudoku.multi.${battleEntity.id}")

    fun FirebaseMessaging.unsubscribeBattle(battleEntity: BattleEntity) =
        unsubscribeFromTopic("sudoku.multi.${battleEntity.id}")
}