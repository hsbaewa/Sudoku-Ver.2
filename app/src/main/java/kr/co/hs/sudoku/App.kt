package kr.co.hs.sudoku

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.games.PlayGamesSdk
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.repository.challenge.ChallengeRepositoryImpl
import kr.co.hs.sudoku.repository.record.ChallengeRecordRepository
import java.lang.ref.SoftReference

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        PlayGamesSdk.initialize(this)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    private var challengeRecordRepositoryRef: SoftReference<ChallengeRecordRepository>? = null
    fun getChallengeRecordRepository(challengeId: String): ChallengeRecordRepository =
        challengeRecordRepositoryRef?.get()
            ?.takeIf { it.challengeId == challengeId }
            ?: ChallengeRecordRepository(challengeId).apply {
                challengeRecordRepositoryRef = SoftReference(this)
            }

    fun clearChallengeRecordRepository() = challengeRecordRepositoryRef?.clear()

    private var challengeRepositoryRef: SoftReference<ChallengeRepository>? = null
    fun getChallengeRepository(): ChallengeRepository =
        challengeRepositoryRef?.get()
            ?: ChallengeRepositoryImpl().apply {
                challengeRepositoryRef = SoftReference(this)
            }

    fun clearChallengeRepository() = challengeRepositoryRef?.clear()
}