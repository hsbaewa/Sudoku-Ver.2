package kr.co.hs.sudoku.feature.user

import androidx.core.net.toUri
import com.google.android.gms.games.GamesSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PlayGamesAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.usecase.UseCase
import kr.co.hs.sudoku.usecase.user.CheckInUseCase
import kr.co.hs.sudoku.usecase.user.CheckOutUseCase
import kr.co.hs.sudoku.usecase.user.CreateProfileUseCase
import kr.co.hs.sudoku.usecase.user.GetProfileUseCase
import kr.co.hs.sudoku.usecase.user.UpdateProfileUseCase
import javax.inject.Inject

class GoogleGamesAuthenticator
@Inject constructor(
    private val gamesSignInClient: GamesSignInClient,
    private val defaultWebClientId: String,
    private val firebaseAuth: FirebaseAuth,
    private val createProfile: CreateProfileUseCase,
    private val getProfile: GetProfileUseCase,
    updateProfile: UpdateProfileUseCase,
    checkIn: CheckInUseCase,
    checkOut: CheckOutUseCase
) : FirebaseAuthenticator(
    firebaseAuth,
    createProfile,
    getProfile,
    updateProfile,
    checkIn,
    checkOut
) {
    override fun signIn(): Flow<ProfileEntity> = callbackFlow {
        gamesSignInClient
            .signIn()
            .await()
            .isAuthenticated
            .takeUnless { it }
            ?.let { throw SignInFailed("play game sign-in failed") }

        val authCode = gamesSignInClient.requestServerSideAccess(defaultWebClientId, false).await()

        val authResult = PlayGamesAuthProvider.getCredential(authCode)
            .run { firebaseAuth.signInWithCredential(this) }
            .await()

        val user = authResult.user
            ?: throw SignInFailed("firebase auth sign-in failed")

        val profile = user.toProfile()

        createProfile(profile, this) {
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

                    user.updateProfile(
                        userProfileChangeRequest {
                            this.displayName = it.data.displayName
                            this.photoUri = it.data.iconUrl?.toUri()
                        }
                    ).await()


                    send(it.data)
                    close()
                }
            }
        }

        awaitClose()
    }

    override fun getProfile(): Flow<ProfileEntity> = callbackFlow {
        gamesSignInClient
            .signIn()
            .await()
            .isAuthenticated
            .takeUnless { it }
            ?.let { throw Authenticator.RequireSignIn("play game sign-in failed") }

        val authCode = gamesSignInClient.requestServerSideAccess(defaultWebClientId, false).await()

        PlayGamesAuthProvider.getCredential(authCode)
            .run { firebaseAuth.signInWithCredential(this) }
            .await()

        super.getProfile()
            .catch { close(it) }
            .collect {
                send(it)
                close()
            }
    }

    class SignInFailed(message: String?) : Exception(message)
}