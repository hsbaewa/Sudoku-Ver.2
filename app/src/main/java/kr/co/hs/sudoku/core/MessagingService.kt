package kr.co.hs.sudoku.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BigTextStyle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.extension.platform.ContextExtension.getMetaData
import kr.co.hs.sudoku.feature.multi.play.MultiPlayActivity
import kr.co.hs.sudoku.model.battle.BattleEntity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import kotlin.random.Random

class MessagingService : FirebaseMessagingService() {
    companion object {
        suspend fun send(serverKey: String, topic: String, data: JsonObject) {
            val response = withContext(Dispatchers.IO) {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://fcm.googleapis.com")
                    .client(
                        OkHttpClient().newBuilder()
                            .addInterceptor {
                                val newRequest = it.request().newBuilder()
                                    .addHeader("Authorization", "key=$serverKey")
                                    .build()
                                it.proceed(newRequest)
                            }
                            .build()
                    )
                    .build()

                val jsonObject = JsonObject()
                jsonObject.addProperty("to", "/topics/$topic")
                jsonObject.add("data", data)

                retrofit.create(APIInterface::class.java).send(
                    Gson().toJson(jsonObject)
                        .toRequestBody(contentType = "application/json".toMediaType())
                )
            }

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                throw Exception(errorBody)
            }
        }

        sealed class Action {
            data class JoinedMultiPlay(val battleId: String, val joinedUserName: String) : Action()
        }

        private fun getJoinedMultiPlayData(battleId: String, userName: String) =
            JsonObject().apply {
                addProperty("action", Action.JoinedMultiPlay::class.java.name)
                addProperty("battleId", battleId)
                addProperty("joinedUserName", userName)
            }

        private fun RemoteMessage.parseAction(): Action? = when (data.get("action")) {
            Action.JoinedMultiPlay::class.java.name -> {
                val battleId = data.get("battleId") ?: throw Exception()
                val joinedUserName = data.get("joinedUserName") ?: throw Exception()
                Action.JoinedMultiPlay(battleId, joinedUserName)
            }

            else -> null
        }

        suspend fun sendJoinedMultiPlay(context: Context, battle: BattleEntity) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val serverKey = context
                .runCatching { getMetaData("kr.co.hs.sudoku.messaging.serverKey") }
                .getOrNull() ?: return

            val topic = "sudoku.multi.${battle.id}"
            val app = context.applicationContext as App
            val profile = app.getProfileRepository().getProfile(uid)
            send(serverKey, topic, getJoinedMultiPlayData(battle.id, profile.displayName))
        }

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        when (val action = message.runCatching { parseAction() }.getOrNull()) {
            is Action.JoinedMultiPlay -> action.showNotification()
            null -> {}
        }
    }

    private interface APIInterface {
        @Headers("Content-Type: application/json")
        @POST("fcm/send")
        suspend fun send(@Body requestBody: RequestBody): Response<ResponseBody>
    }

    private fun Action.JoinedMultiPlay.showNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createJoinedMultiPlayNotificationChannel()
        }
        notificationManager.notify(Random.nextInt(), getNotification())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createJoinedMultiPlayNotificationChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = getString(R.string.channel_id_multi_play_joined_participant)
        val channelName = getString(R.string.channel_name_multi_play_joined_participant)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
        )
    }

    private fun Action.JoinedMultiPlay.getNotification(): Notification {
        val channelId = getString(R.string.channel_id_multi_play_joined_participant)
        val contentText =
            getString(R.string.multi_play_notification_joined_participant, joinedUserName)
        val largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        val contentIntent = MultiPlayActivity.newIntent(this@MessagingService, battleId)
        val pIntent = PendingIntent.getActivity(
            this@MessagingService,
            100,
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )
        return NotificationCompat.Builder(this@MessagingService, channelId)
            .setContentTitle(getString(R.string.app_name))
            .setTicker(contentText)
            .setContentText(contentText)
            .setStyle(BigTextStyle().bigText(contentText))
            .setSmallIcon(R.drawable.ic_stat_name)
            .setLargeIcon(largeIcon)
            .setAutoCancel(true)
            .setContentIntent(pIntent)
            .build()
    }
}