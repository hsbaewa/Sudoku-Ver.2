package kr.co.hs.sudoku.feature.user

import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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

abstract class FirebaseAuthenticator(
    private val firebaseAuth: FirebaseAuth,
    private val createProfile: CreateProfileUseCase,
    private val getProfile: GetProfileUseCase,
    private val updateProfile: UpdateProfileUseCase,
    private val checkIn: CheckInUseCase,
    private val checkOut: CheckOutUseCase
) : Authenticator {

    protected fun FirebaseUser.toProfile(): ProfileEntity = ProfileEntityImpl(
        uid = uid,
        displayName = displayName ?: "",
        message = null,
        iconUrl = photoUrl?.toString() ?: "",
        locale = LocaleEntityImpl(
            Locale.getDefault().language,
            Locale.getDefault().country
        ),
        lastCheckedAt = null
    )

    open val getCurrentUserProfile: ProfileEntity?
        get() = firebaseAuth.currentUser?.toProfile()

    override fun getProfile(): Flow<ProfileEntity> = callbackFlow {
        val currentUserProfile =
            getCurrentUserProfile ?: throw Authenticator.RequireSignIn("unknown user")

        getProfile(currentUserProfile.uid, this) {
            when (it) {
                is UseCase.Result.Error -> when (it.e) {
                    GetProfileUseCase.EmptyUserId -> close(IllegalArgumentException("user id is empty"))
                    GetProfileUseCase.ProfileNotFound -> createProfile(currentUserProfile, this) {
                        when (it) {
                            is UseCase.Result.Error -> when (it.e) {
                                CreateProfileUseCase.AlreadyUser -> close(Exception("invalid state exception"))
                                CreateProfileUseCase.EmptyUserId -> close(Exception("invalid state exception"))
                            }

                            is UseCase.Result.Exception -> close(it.t)
                            is UseCase.Result.Success -> launch {
                                send(it.data)
                                close()
                            }
                        }
                    }
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

    override fun checkIn(): Flow<ProfileEntity> = callbackFlow {
        val user = getCurrentUserProfile ?: throw Exception("unknown user")

        checkIn(user.uid, this) {
            when (it) {
                is UseCase.Result.Error -> when (it.e) {
                    CheckInUseCase.EmptyUserId -> close(Exception("invalid user"))
                    CheckInUseCase.UnKnownUser -> close(Exception("unknown user"))
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

    override fun checkOut(): Flow<ProfileEntity> = callbackFlow {
        val user = getCurrentUserProfile ?: throw Exception("unknown user")

        checkOut(user.uid, this) {
            when (it) {
                is UseCase.Result.Error -> when (it.e) {
                    CheckOutUseCase.EmptyUserId -> close(Exception("invalid user"))
                    CheckOutUseCase.UnKnownUser -> close(Exception("unknown user"))
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

    override fun updateProfile(profileEntity: ProfileEntity): Flow<ProfileEntity> = callbackFlow {
        updateProfile(profileEntity, this) {
            when (it) {
                is UseCase.Result.Error -> when (it.e) {
                    UpdateProfileUseCase.EmptyUserId -> close(Exception("invalid user"))
                    UpdateProfileUseCase.ProfileNotFound -> close(Exception("unknown user"))
                }

                is UseCase.Result.Exception -> close(it.t)
                is UseCase.Result.Success -> launch {
                    send(it.data)
                    firebaseAuth.currentUser?.updateProfile(
                        userProfileChangeRequest {
                            this.displayName = it.data.displayName
                            this.photoUri = it.data.iconUrl?.toUri()
                        }
                    )?.await()
                    close()
                }
            }
        }

        awaitClose()
    }
}