package kr.co.hs.sudoku.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.feature.messaging.MessagingManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    @MessagingManagerQualifier
    fun provideMessagingManager(@ApplicationContext context: Context): MessagingManager =
        MessagingManager(context as App)
}