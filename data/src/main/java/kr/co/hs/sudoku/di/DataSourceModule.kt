package kr.co.hs.sudoku.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.hs.sudoku.datasource.challenge.ChallengeRecordRemoteSource
import kr.co.hs.sudoku.datasource.challenge.ChallengeRemoteSource
import kr.co.hs.sudoku.datasource.challenge.impl.ChallengeRecordRemoteSourceImpl
import kr.co.hs.sudoku.datasource.challenge.impl.ChallengeRemoteSourceImpl
import kr.co.hs.sudoku.datasource.logs.LogRemoteSource
import kr.co.hs.sudoku.datasource.logs.impl.LogRemoteSourceImpl
import kr.co.hs.sudoku.datasource.user.ProfileDataSource
import kr.co.hs.sudoku.datasource.user.ProfileRemoteSource
import kr.co.hs.sudoku.datasource.user.impl.ProfileDataSourceImpl
import kr.co.hs.sudoku.datasource.user.impl.ProfileRemoteSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindProfileRemoteSource(remoteSource: ProfileRemoteSourceImpl): ProfileRemoteSource

    @Binds
    @Singleton
    abstract fun bindProfileDataSource(dataSource: ProfileDataSourceImpl): ProfileDataSource

    @Binds
    @Singleton
    abstract fun bindLogRemoteSource(remoteSource: LogRemoteSourceImpl): LogRemoteSource

    @Binds
    @Singleton
    abstract fun bindChallengeRemoteSource(remoteSource: ChallengeRemoteSourceImpl): ChallengeRemoteSource

    @Binds
    @Singleton
    abstract fun bindChallengeRecordRemoteSource(remoteSource: ChallengeRecordRemoteSourceImpl): ChallengeRecordRemoteSource
}