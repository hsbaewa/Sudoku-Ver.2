package kr.co.hs.sudoku

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.games.PlayGamesSdk
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import kr.co.hs.sudoku.extension.FirebaseCloudMessagingExt.subscribeAllUser
import kr.co.hs.sudoku.extension.StringExt.md5

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        PlayGamesSdk.initialize(this)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val adRequestConfiguration = RequestConfiguration.Builder()
        if (BuildConfig.DEBUG) {
            @SuppressLint("HardwareIds")
            val deviceId =
                Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)?.md5
            deviceId?.run { adRequestConfiguration.setTestDeviceIds(listOf(this)) }
        }
        MobileAds.setRequestConfiguration(adRequestConfiguration.build())
        MobileAds.initialize(this)

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        FirebaseMessaging.getInstance().subscribeAllUser()
    }
}