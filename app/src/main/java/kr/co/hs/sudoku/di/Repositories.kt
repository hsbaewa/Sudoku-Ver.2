package kr.co.hs.sudoku.di

import kr.co.hs.sudoku.repository.ChallengeRankingRepository
import kr.co.hs.sudoku.repository.ProfileRepositoryImpl
import kr.co.hs.sudoku.repository.rank.RankingRepository
import kr.co.hs.sudoku.repository.user.ProfileRepository
import java.lang.ref.WeakReference

object Repositories {
    private var weakRefProfileRepository: WeakReference<ProfileRepository>? = null
    fun getProfileRepository() = weakRefProfileRepository
        ?.get()
        ?: ProfileRepositoryImpl().also { weakRefProfileRepository = WeakReference(it) }

    private var weakRefChallengeRankingRepository: WeakReference<RankingRepository>? = null
    fun getChallengeRankingRepository() = weakRefChallengeRankingRepository
        ?.get()
        ?: ChallengeRankingRepository("2023-04-18")
            .also { weakRefChallengeRankingRepository = WeakReference(it) }
}