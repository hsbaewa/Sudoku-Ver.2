package kr.co.hs.sudoku

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.games.PlayGamesSdk
import kr.co.hs.sudoku.extension.StringExt.md5
import kr.co.hs.sudoku.repository.ProfileRepositoryImpl
import kr.co.hs.sudoku.repository.battle.BattleRepository
import kr.co.hs.sudoku.repository.battle.BattleRepositoryImpl
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.repository.challenge.ChallengeRepositoryImpl
import kr.co.hs.sudoku.repository.user.ProfileRepository
import java.lang.ref.SoftReference

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        PlayGamesSdk.initialize(this)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        @SuppressLint("HardwareIds")
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)?.md5
        val adRequestConfiguration = RequestConfiguration.Builder()
        deviceId?.run { adRequestConfiguration.setTestDeviceIds(listOf(this)) }
        MobileAds.setRequestConfiguration(adRequestConfiguration.build())
        MobileAds.initialize(this)
    }

    private var challengeRepositoryRef: SoftReference<ChallengeRepository>? = null
    fun getChallengeRepository(): ChallengeRepository =
        challengeRepositoryRef?.get()
            ?: ChallengeRepositoryImpl().apply {
                challengeRepositoryRef = SoftReference(this)
            }

    private var profileRepositoryRef: SoftReference<ProfileRepository>? = null
    fun getProfileRepository(): ProfileRepository =
        profileRepositoryRef?.get()
            ?: ProfileRepositoryImpl().apply { profileRepositoryRef = SoftReference(this) }

    private var battleRepositoryRef: SoftReference<BattleRepository>? = null
    fun getBattleRepository(): BattleRepository =
        battleRepositoryRef?.get()
            ?: BattleRepositoryImpl()
                .apply { battleRepositoryRef = SoftReference(this) }
}