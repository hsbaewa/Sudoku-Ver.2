package kr.co.hs.sudoku.di.google

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.co.hs.sudoku.R
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GoogleApplicationModule {
    @GoogleDefaultWebClientIdQualifier
    @Singleton
    @Provides
    fun provideDefaultWebClientId(@ApplicationContext context: Context): String {
        return context.getString(R.string.default_web_client_id)
    }
}