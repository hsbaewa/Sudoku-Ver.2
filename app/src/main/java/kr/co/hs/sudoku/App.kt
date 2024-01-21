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
import kr.co.hs.sudoku.extension.FirebaseCloudMessagingExt.subscribeAllUser
import kr.co.hs.sudoku.extension.StringExt.md5
import kr.co.hs.sudoku.extension.platform.ContextExtension.dataStore
import kr.co.hs.sudoku.repository.GameSettingsRepositoryImpl
import kr.co.hs.sudoku.repository.ProfileRepositoryImpl
import kr.co.hs.sudoku.repository.RegistrationRepositoryImpl
import kr.co.hs.sudoku.repository.admin.AdminPermissionRepository
import kr.co.hs.sudoku.repository.admin.AdminPermissionRepositoryImpl
import kr.co.hs.sudoku.repository.battle.BattleRepository
import kr.co.hs.sudoku.repository.battle.BattleRepositoryImpl
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.repository.challenge.ChallengeRepositoryImpl
import kr.co.hs.sudoku.repository.settings.GameSettingsRepository
import kr.co.hs.sudoku.repository.settings.RegistrationRepository
import kr.co.hs.sudoku.repository.user.ProfileRepository
import java.lang.ref.Reference
import java.lang.ref.SoftReference

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

    private var challengeRepositoryRef: Reference<ChallengeRepository>? = null
    fun getChallengeRepository(): ChallengeRepository =
        challengeRepositoryRef?.get()
            ?: ChallengeRepositoryImpl().apply { challengeRepositoryRef = SoftReference(this) }

    fun clearChallengeRepository() = getChallengeRepository().clearCache()

    private var profileRepositoryRef: Reference<ProfileRepository>? = null
    fun getProfileRepository(): ProfileRepository =
        profileRepositoryRef?.get()
            ?: ProfileRepositoryImpl().apply { profileRepositoryRef = SoftReference(this) }

    private var battleRepositoryRef: Reference<BattleRepository>? = null
    fun getBattleRepository(): BattleRepository =
        battleRepositoryRef?.get()
            ?: BattleRepositoryImpl()
                .apply { battleRepositoryRef = SoftReference(this) }

    private var gameSettingsRepositoryRef: Reference<GameSettingsRepository>? = null
    fun getGameSettingsRepository(): GameSettingsRepository =
        gameSettingsRepositoryRef?.get()
            ?: GameSettingsRepositoryImpl(dataStore)
                .apply { gameSettingsRepositoryRef = SoftReference(this) }

    private var registrationRepositoryRef: Reference<RegistrationRepository>? = null
    fun getRegistrationRepository(): RegistrationRepository =
        registrationRepositoryRef?.get()
            ?: RegistrationRepositoryImpl(dataStore)
                .apply { registrationRepositoryRef = SoftReference(this) }

    private var adminPermissionRepositoryRef: Reference<AdminPermissionRepository>? = null
    fun getAdminPermissionRepository(): AdminPermissionRepository =
        adminPermissionRepositoryRef?.get()
            ?: AdminPermissionRepositoryImpl()
                .apply { adminPermissionRepositoryRef = SoftReference(this) }
}