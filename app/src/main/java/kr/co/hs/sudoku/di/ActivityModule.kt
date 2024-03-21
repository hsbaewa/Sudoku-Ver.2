package kr.co.hs.sudoku.di

import android.app.Activity
import android.content.Context
import com.google.android.gms.games.PlayGames
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.feature.user.Authenticator
import kr.co.hs.sudoku.feature.user.GoogleGamesAuthenticator
import kr.co.hs.sudoku.usecase.user.CheckInUseCase
import kr.co.hs.sudoku.usecase.user.CheckOutUseCase
import kr.co.hs.sudoku.usecase.user.CreateProfileUseCase
import kr.co.hs.sudoku.usecase.user.GetProfileUseCase
import kr.co.hs.sudoku.usecase.user.UpdateProfileUseCase

@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {

    @Provides
    fun bindGoogleGamesAuthenticator(
        @ActivityContext context: Context,
        createProfile: CreateProfileUseCase,
        getProfile: GetProfileUseCase,
        updateProfile: UpdateProfileUseCase,
        checkIn: CheckInUseCase,
        checkOut: CheckOutUseCase
    ): Authenticator = GoogleGamesAuthenticator(
        PlayGames.getGamesSignInClient(context as Activity),
        context.getString(R.string.default_web_client_id),
        FirebaseAuth.getInstance(),
        createProfile,
        getProfile,
        updateProfile,
        checkIn,
        checkOut
    )
}