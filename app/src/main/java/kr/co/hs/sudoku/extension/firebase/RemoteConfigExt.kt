package kr.co.hs.sudoku.extension.firebase

import android.app.Activity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await

object RemoteConfigExt {
    suspend fun Activity.fetchAndActivate() =
        FirebaseRemoteConfig.getInstance().fetchAndActivate().await()
}