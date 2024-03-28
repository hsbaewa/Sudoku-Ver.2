package kr.co.hs.sudoku.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.hs.sudoku.datasource.challenge.ChallengeRecordRemoteSource
import kr.co.hs.sudoku.datasource.challenge.ChallengeRemoteSource
import kr.co.hs.sudoku.datasource.logs.LogRemoteSource
import kr.co.hs.sudoku.datasource.user.ProfileDataSource
import kr.co.hs.sudoku.datasource.user.ProfileRemoteSource
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.repository.challenge.ChallengeRepositoryImpl
import kr.co.hs.sudoku.repository.history.HistoryRepository
import kr.co.hs.sudoku.repository.history.HistoryRepositoryImpl
import kr.co.hs.sudoku.repository.history.MyHistoryRepository
import kr.co.hs.sudoku.repository.history.MyHistoryRepositoryImpl
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.repository.user.ProfileRepositoryImpl
import kr.co.hs.sudoku.usecase.challenge.GetChallengeUseCase
import kr.co.hs.sudoku.usecase.user.GetCurrentUserProfileUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun bindProfileRepository(
        profileDataSource: ProfileDataSource,
        profileRemoteSource: ProfileRemoteSource
    ): ProfileRepository = ProfileRepositoryImpl(
        profileDataSource,
        profileRemoteSource,
        FirebaseAuth.getInstance()
    )

    @Provides
    @Singleton
    fun bindChallengeRepository(
        challengeRemoteSource: ChallengeRemoteSource,
        recordRemoteSource: ChallengeRecordRemoteSource,
        logRemoteSource: LogRemoteSource,
        getCurrentUserProfileUseCase: GetCurrentUserProfileUseCase
    ): ChallengeRepository = ChallengeRepositoryImpl(
        challengeRemoteSource,
        recordRemoteSource,
        logRemoteSource,
        getCurrentUserProfileUseCase
    )

    @Provides
    @Singleton
    fun bindHistoryRepository(
        getChallenge: GetChallengeUseCase,
        logRemoteSource: LogRemoteSource,
        recordRemoteSource: ChallengeRecordRemoteSource
    ): HistoryRepository = HistoryRepositoryImpl(
        getChallenge,
        logRemoteSource,
        recordRemoteSource,
    )

    @Provides
    @Singleton
    fun bindMyHistoryRepository(
        getChallenge: GetChallengeUseCase,
        logRemoteSource: LogRemoteSource,
        recordRemoteSource: ChallengeRecordRemoteSource,
        getCurrentUserProfileUseCase: GetCurrentUserProfileUseCase
    ): MyHistoryRepository = MyHistoryRepositoryImpl(
        getChallenge,
        logRemoteSource,
        recordRemoteSource,
        getCurrentUserProfileUseCase
    )
}