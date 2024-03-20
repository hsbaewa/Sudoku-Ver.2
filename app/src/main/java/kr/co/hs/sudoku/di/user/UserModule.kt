package kr.co.hs.sudoku.di.user

import com.google.android.gms.games.GamesSignInClient
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import kr.co.hs.sudoku.di.google.GoogleDefaultWebClientIdQualifier
import kr.co.hs.sudoku.di.google.GoogleGameSignInClientQualifier
import kr.co.hs.sudoku.feature.user.Authenticator
import kr.co.hs.sudoku.feature.user.GoogleGamesAuthenticator
import kr.co.hs.sudoku.usecase.user.CheckInUseCase
import kr.co.hs.sudoku.usecase.user.CheckOutUseCase
import kr.co.hs.sudoku.usecase.user.CreateProfileUseCase
import kr.co.hs.sudoku.usecase.user.GetProfileUseCase
import kr.co.hs.sudoku.usecase.user.UpdateProfileUseCase
import javax.inject.Qualifier

@Module
@InstallIn(ActivityComponent::class)
object UserModule {

    @Qualifier
    annotation class GoogleGamesAuthenticatorQualifier

    @Provides
    @GoogleGamesAuthenticatorQualifier
    fun provideGoogleGamesAuthenticator(
        @GoogleGameSignInClientQualifier gamesSignInClient: GamesSignInClient,
        @GoogleDefaultWebClientIdQualifier defaultWebClientId: String,
        createProfileUseCase: CreateProfileUseCase,
        getProfileUseCase: GetProfileUseCase,
        updateProfileUseCase: UpdateProfileUseCase,
        checkInUseCase: CheckInUseCase,
        checkOutUseCase: CheckOutUseCase
    ): Authenticator = GoogleGamesAuthenticator(
        gamesSignInClient,
        defaultWebClientId,
        FirebaseAuth.getInstance(),
        createProfileUseCase,
        getProfileUseCase,
        updateProfileUseCase,
        checkInUseCase,
        checkOutUseCase
    )
}