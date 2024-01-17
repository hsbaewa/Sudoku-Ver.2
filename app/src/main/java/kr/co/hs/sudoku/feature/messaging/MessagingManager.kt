package kr.co.hs.sudoku.feature.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.extension.platform.ContextExtension.getColorCompat
import kr.co.hs.sudoku.extension.platform.ContextExtension.getMetaData
import kr.co.hs.sudoku.feature.MainActivity
import kr.co.hs.sudoku.feature.multi.play.MultiPlayActivity
import okhttp3.Interceptor
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
import java.util.Date
import kotlin.math.abs
import kotlin.random.Random

class MessagingManager(private val app: App) {
    sealed interface Action {
        companion object {
            fun RemoteMessage.parseAction(): Action? =
                when (data.takeIf { it.containsKey("action") }?.get("action")) {
                    "joined_multi_player" -> runCatching {
                        val battleId = data
                            .takeIf { it.containsKey("battle_id") }
                            ?.get("battle_id")
                            ?: throw Exception("battle_id is null")
                        val joinedUserName = data
                            .takeIf { it.containsKey("joined_user_name") }
                            ?.get("joined_user_name")
                            ?: throw Exception("joined_user_name is null")
                        JoinedMultiPlayer(battleId, joinedUserName)
                    }.getOrNull()

                    "app_update" -> runCatching {
                        val versionName = data
                            .takeIf { it.containsKey("version_name") }
                            ?.get("version_name")
                            ?: throw Exception("version_name is null")
                        val versionCode = data
                            .takeIf { it.containsKey("version_code") }
                            ?.get("version_code")
                            ?.toLongOrNull()
                            ?: throw Exception("version_name is null")
                        AppUpdate(versionName, versionCode)
                    }.getOrNull()

                    "new_challenge" -> runCatching {
                        val createdAt = data
                            .takeIf { it.containsKey("created_at") }
                            ?.get("created_at")
                            ?.toLongOrNull()
                            ?: throw Exception("created_at is null")
                        NewChallenge(Date(createdAt))
                    }.getOrNull()

                    else -> null
                }
        }

        fun toJsonObject(): JsonObject
        fun showNotification(context: Context)
    }

    data class JoinedMultiPlayer(
        val battleId: String,
        val joinedUserName: String
    ) : Action {
        override fun toJsonObject() = JsonObject().apply {
            addProperty("action", "joined_multi_player")
            addProperty("battle_id", battleId)
            addProperty("joined_user_name", joinedUserName)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun NotificationManager.createNotificationChannel(context: Context) {
            val channelId = context.getString(R.string.channel_id_multi_play_joined_participant)
            val channelName = context.getString(R.string.channel_name_multi_play_joined_participant)
            createNotificationChannel(
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            )
        }

        override fun showNotification(context: Context) {
            val notificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(context)
            }

            val channelId = context.getString(R.string.channel_id_multi_play_joined_participant)
            val title = context.getString(R.string.app_name)
            val contentText = context.getString(
                R.string.multi_play_notification_joined_participant,
                joinedUserName
            )
            val requestCode = Random.nextInt()
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
            val contentIntent = MultiPlayActivity.newIntent(context, battleId)
            val pIntent = PendingIntent.getActivity(
                context, requestCode, contentIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
            )
            notificationManager.notify(
                requestCode,
                NotificationCompat.Builder(context, channelId)
                    .setContentTitle(title)
                    .setTicker(contentText)
                    .setContentText(contentText)
                    .setColor(context.getColorCompat(R.color.gray_700))
                    .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setLargeIcon(largeIcon)
                    .setAutoCancel(true)
                    .setContentIntent(pIntent)
                    .build()
            )
        }
    }

    data class AppUpdate(
        val versionName: String,
        val versionCode: Long
    ) : Action {
        override fun toJsonObject() = JsonObject().apply {
            addProperty("action", "app_update")
            addProperty("version_name", versionName)
            addProperty("version_code", versionCode.toString())
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun NotificationManager.createNotificationChannel(context: Context) {
            val channelId = context.getString(R.string.channel_id_app_update)
            val channelName = context.getString(R.string.channel_name_app_update)
            createNotificationChannel(
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            )
        }

        override fun showNotification(context: Context) {
            val notificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(context)
            }

            val channelId = context.getString(R.string.channel_id_app_update)
            val title = context.getString(R.string.notification_title_app_update)
            val contentText =
                context.getString(R.string.notification_contents_app_update, versionName)
            val requestCode = Random.nextInt()
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
            val contentIntent = MainActivity.newIntentForUpdate(context)
            val pIntent = PendingIntent.getActivity(
                context, requestCode, contentIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
            )
            notificationManager.notify(
                requestCode,
                NotificationCompat.Builder(context, channelId)
                    .setContentTitle(title)
                    .setTicker(contentText)
                    .setContentText(contentText)
                    .setColor(context.getColorCompat(R.color.gray_700))
                    .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setLargeIcon(largeIcon)
                    .setAutoCancel(true)
                    .setContentIntent(pIntent)
                    .build()
            )
        }
    }

    data class NewChallenge(val createdAt: Date) : Action {
        override fun toJsonObject() = JsonObject().apply {
            addProperty("action", "new_challenge")
            addProperty("created_at", createdAt.time.toString())
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun NotificationManager.createNotificationChannel(context: Context) {
            val channelId = context.getString(R.string.channel_id_new_challenge)
            val channelName = context.getString(R.string.channel_name_new_challenge)
            createNotificationChannel(
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            )
        }

        override fun showNotification(context: Context) {
            val notificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(context)
            }

            val channelId = context.getString(R.string.channel_id_new_challenge)
            val title = context.getString(R.string.notification_title_new_challenge)
            val contentText = context.getString(R.string.notification_contents_new_challenge)
            val requestCode = abs(Random.nextInt())
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
            val contentIntent = MainActivity.newIntentForNewChallenge(context)
            val pIntent = PendingIntent.getActivity(
                context, requestCode, contentIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
            )
            notificationManager.notify(
                requestCode,
                NotificationCompat.Builder(context, channelId)
                    .setContentTitle(title)
                    .setTicker(contentText)
                    .setContentText(contentText)
                    .setColor(context.getColorCompat(R.color.gray_700))
                    .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setLargeIcon(largeIcon)
                    .setAutoCancel(true)
                    .setContentIntent(pIntent)
                    .build()
            )
        }
    }

    private interface NetworkInterface {
        @Headers("Content-Type: application/json")
        @POST("fcm/send")
        suspend fun send(@Body requestBody: RequestBody): Response<ResponseBody>
    }

    private val serverKey: String by lazy {
        app.runCatching { getMetaData("kr.co.hs.sudoku.messaging.serverKey") }
            .getOrNull() ?: ""
    }
    private val interceptor: Interceptor by lazy {
        Interceptor {
            val newRequest = it.request().newBuilder()
                .addHeader("Authorization", "key=$serverKey")
                .build()
            it.proceed(newRequest)
        }
    }
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient().newBuilder()
            .addInterceptor(interceptor)
            .build()
    }
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com")
            .client(httpClient)
            .build()
    }

    private suspend fun sendData(topic: String, data: JsonObject) {
        val response = withContext(Dispatchers.IO) {
            retrofit
                .create(NetworkInterface::class.java)
                .send(
                    Gson()
                        .toJson(
                            JsonObject().apply {
                                addProperty("to", "/topics/$topic")
                                add("data", data)
                            }
                        )
                        .toRequestBody(contentType = "application/json".toMediaType())
                )
        }

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            throw Exception(errorBody)
        }
    }

    suspend fun sendNotification(action: JoinedMultiPlayer) = sendData(
        "sudoku.multi.${action.battleId}",
        action.toJsonObject()
    )

    suspend fun sendNotification(appUpdate: AppUpdate) = sendData(
        "sudoku.user.all",
        appUpdate.toJsonObject()
    )
}