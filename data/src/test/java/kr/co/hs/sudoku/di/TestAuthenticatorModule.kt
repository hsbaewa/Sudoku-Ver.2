package kr.co.hs.sudoku.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.hs.sudoku.feature.user.Authenticator
import kr.co.hs.sudoku.feature.user.TestFirebaseAuthenticator
import kr.co.hs.sudoku.usecase.user.CheckInUseCase
import kr.co.hs.sudoku.usecase.user.CheckOutUseCase
import kr.co.hs.sudoku.usecase.user.CreateProfileUseCase
import kr.co.hs.sudoku.usecase.user.GetProfileUseCase
import kr.co.hs.sudoku.usecase.user.UpdateProfileUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestAuthenticatorModule {
    @Provides
    @Singleton
    fun bindTestFirebaseAuthenticator(
        createProfile: CreateProfileUseCase,
        getProfile: GetProfileUseCase,
        updateProfile: UpdateProfileUseCase,
        checkIn: CheckInUseCase,
        checkOut: CheckOutUseCase
    ): Authenticator = TestFirebaseAuthenticator(
        FirebaseAuth.getInstance(),
        createProfile,
        getProfile,
        updateProfile,
        checkIn,
        checkOut
    )
}