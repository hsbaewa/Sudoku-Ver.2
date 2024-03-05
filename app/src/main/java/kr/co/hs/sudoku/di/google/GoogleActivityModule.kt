package kr.co.hs.sudoku.di.google

import android.app.Activity
import android.content.Context
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.PlayGames
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(ActivityComponent::class)
object GoogleActivityModule {
    @GoogleGameSignInClientQualifier
    @Provides
    fun provideGameSignInClient(@ActivityContext context: Context): GamesSignInClient {
        return PlayGames.getGamesSignInClient(context as Activity)
    }
}