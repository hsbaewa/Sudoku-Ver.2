package kr.co.hs.sudoku.feature.user

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.impl.LocaleEntityImpl
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl
import kr.co.hs.sudoku.usecase.UseCase
import kr.co.hs.sudoku.usecase.user.CheckInUseCase
import kr.co.hs.sudoku.usecase.user.CheckOutUseCase
import kr.co.hs.sudoku.usecase.user.CreateProfileUseCase
import kr.co.hs.sudoku.usecase.user.GetProfileUseCase
import kr.co.hs.sudoku.usecase.user.UpdateProfileUseCase
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnonymousAuthenticator
@Inject constructor(
    firebaseAuth: FirebaseAuth,
    private val createProfile: CreateProfileUseCase,
    private val getProfile: GetProfileUseCase,
    updateProfile: UpdateProfileUseCase,
    checkIn: CheckInUseCase,
    checkOut: CheckOutUseCase
) : Authenticator(firebaseAuth, createProfile, getProfile, updateProfile, checkIn, checkOut) {


    override fun signIn(): Flow<ProfileEntity> = callbackFlow {
        val user = ProfileEntityImpl(
            uid = "profile-uid",
            displayName = "display-name",
            message = null,
            iconUrl = "http://img.url",
            locale = LocaleEntityImpl(
                Locale.getDefault().language,
                Locale.getDefault().country
            ),
            lastCheckedAt = null
        )

        createProfile(user, this) {
            when (it) {
                is UseCase.Result.Error -> when (it.e) {
                    CreateProfileUseCase.AlreadyUser -> getProfile(user.uid, this) {
                        when (it) {
                            is UseCase.Result.Error -> when (it.e) {
                                GetProfileUseCase.EmptyUserId -> close(IllegalArgumentException("user id is empty"))
                                GetProfileUseCase.ProfileNotFound -> close(kotlin.Exception("invalid state exception"))
                            }

                            is UseCase.Result.Exception -> close(it.t)
                            is UseCase.Result.Success -> launch {
                                send(it.data)
                                close()
                            }
                        }
                    }

                    CreateProfileUseCase.EmptyUserId -> close(IllegalArgumentException("user id is empty"))
                }

                is UseCase.Result.Exception -> close(it.t)
                is UseCase.Result.Success -> launch {
                    send(it.data)
                    close()
                }
            }
        }

        awaitClose()
    }
}