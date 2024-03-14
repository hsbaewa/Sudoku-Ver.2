package kr.co.hs.sudoku.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.repository.user.ProfileRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @ProfileRepositoryQualifier
    @Binds
    @Singleton
    abstract fun bindProfileRepository(repository: ProfileRepositoryImpl): ProfileRepository
}