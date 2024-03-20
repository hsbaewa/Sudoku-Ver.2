package kr.co.hs.sudoku.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kr.co.hs.sudoku.di.user.UserModule
import kr.co.hs.sudoku.feature.user.AnonymousAuthenticator
import kr.co.hs.sudoku.feature.user.Authenticator
import kr.co.hs.sudoku.usecase.user.CheckInUseCase
import kr.co.hs.sudoku.usecase.user.CheckOutUseCase
import kr.co.hs.sudoku.usecase.user.CreateProfileUseCase
import kr.co.hs.sudoku.usecase.user.GetProfileUseCase
import kr.co.hs.sudoku.usecase.user.UpdateProfileUseCase
import javax.inject.Singleton


@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [UserModule::class]
)
object TestUserModule {

    @Provides
    @Singleton
    @UserModule.GoogleGamesAuthenticatorQualifier
    fun provideAuthenticator(
        createProfileUseCase: CreateProfileUseCase,
        getProfileUseCase: GetProfileUseCase,
        updateProfileUseCase: UpdateProfileUseCase,
        checkInUseCase: CheckInUseCase,
        checkOutUseCase: CheckOutUseCase
    ): Authenticator = AnonymousAuthenticator(
        FirebaseAuth.getInstance(),
        createProfileUseCase,
        getProfileUseCase,
        updateProfileUseCase,
        checkInUseCase,
        checkOutUseCase
    )
}