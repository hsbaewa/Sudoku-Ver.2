package kr.co.hs.sudoku.core

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.feature.messaging.MessagingManager
import kr.co.hs.sudoku.feature.messaging.MessagingManager.Action.Companion.parseAction

class MessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "Sudoku.MessagingService"
        private fun debug(message: String) = Log.d(TAG, message)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        when (val action = message.parseAction()) {
            is MessagingManager.JoinedMultiPlayer -> action.showNotification(this)
            is MessagingManager.AppUpdate -> action.showNotification(this)
            is MessagingManager.NewChallenge -> {
                val app = applicationContext as App
                app.clearChallengeRepository()
                action.showNotification(this)
            }

            null -> {}
        }
    }
}