package kr.co.hs.sudoku.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kr.co.hs.sudoku.datasource.challenge.ChallengeRecordRemoteSource
import kr.co.hs.sudoku.datasource.challenge.ChallengeRemoteSource
import kr.co.hs.sudoku.datasource.logs.LogRemoteSource
import kr.co.hs.sudoku.datasource.user.ProfileDataSource
import kr.co.hs.sudoku.datasource.user.ProfileRemoteSource
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.repository.user.TestChallengeRepository
import kr.co.hs.sudoku.repository.user.TestProfileRepository
import kr.co.hs.sudoku.usecase.user.GetCurrentUserProfileUseCase
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
object TestRepositoryModule {

    @Provides
    @Singleton
    fun bindProfileRepository(
        profileDataSource: ProfileDataSource,
        profileRemoteSource: ProfileRemoteSource
    ): ProfileRepository = TestProfileRepository(
        profileDataSource,
        profileRemoteSource
    )

    @Provides
    @Singleton
    fun bindChallengeRepository(
        challengeRemoteSource: ChallengeRemoteSource,
        recordRemoteSource: ChallengeRecordRemoteSource,
        logRemoteSource: LogRemoteSource,
        getCurrentUserProfileUseCase: GetCurrentUserProfileUseCase
    ): ChallengeRepository = TestChallengeRepository(
        challengeRemoteSource,
        recordRemoteSource,
        logRemoteSource,
        getCurrentUserProfileUseCase,
        "profile-uid"
    )
}