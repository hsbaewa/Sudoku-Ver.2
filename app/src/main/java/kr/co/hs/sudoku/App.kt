package kr.co.hs.sudoku

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.games.PlayGamesSdk
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
    }

    private var challengeRepositoryRef: SoftReference<ChallengeRepository>? = null
    fun getChallengeRepository(): ChallengeRepository =
        challengeRepositoryRef?.get()
            ?: ChallengeRepositoryImpl().apply {
                challengeRepositoryRef = SoftReference(this)
            }

    fun clearChallengeRepository() = challengeRepositoryRef?.clear()

    private var profileRepositoryRef: SoftReference<ProfileRepository>? = null
    fun getProfileRepository(): ProfileRepository =
        profileRepositoryRef?.get()
            ?: ProfileRepositoryImpl().apply { profileRepositoryRef = SoftReference(this) }

    private var battleRepository2Ref: SoftReference<BattleRepository>? = null
    fun getBattleRepository2(): BattleRepository =
        battleRepository2Ref?.get()
            ?: BattleRepositoryImpl()
                .apply { battleRepository2Ref = SoftReference(this) }
}