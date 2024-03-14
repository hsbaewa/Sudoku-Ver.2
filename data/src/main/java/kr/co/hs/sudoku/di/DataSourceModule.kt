package kr.co.hs.sudoku.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.hs.sudoku.datasource.user.ProfileDataSource
import kr.co.hs.sudoku.datasource.user.ProfileRemoteSource
import kr.co.hs.sudoku.datasource.user.impl.ProfileDataSourceImpl
import kr.co.hs.sudoku.datasource.user.impl.ProfileRemoteSourceImpl
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Qualifier
    annotation class ProfileRemoteSourceQualifier

    @ProfileRemoteSourceQualifier
    @Binds
    @Singleton
    abstract fun bindProfileRemoteSource(remoteSource: ProfileRemoteSourceImpl): ProfileRemoteSource

    @Qualifier
    annotation class ProfileDataSourceQualifier

    @ProfileDataSourceQualifier
    @Binds
    @Singleton
    abstract fun bindProfileDataSource(dataSource: ProfileDataSourceImpl): ProfileDataSource
}