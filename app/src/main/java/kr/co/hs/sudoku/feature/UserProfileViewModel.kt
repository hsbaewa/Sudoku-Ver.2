package kr.co.hs.sudoku.feature

import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.games.GamesSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PlayGamesAuthProvider
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.impl.LocaleEntityImpl
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl
import kr.co.hs.sudoku.repository.ProfileRepositoryImpl
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.viewmodel.ViewModel
import java.util.Locale

class UserProfileViewModel(
    private val gamesSignInClient: GamesSignInClient,
    private val defaultWebClientId: String
) : ViewModel() {
    class ProviderFactory(
        private val gamesSignInClient: GamesSignInClient,
        private val defaultWebClientId: String
    ) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                UserProfileViewModel(gamesSignInClient, defaultWebClientId) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }

    private val profileRepository: ProfileRepository by lazy { ProfileRepositoryImpl() }
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val _profile = MutableLiveData<ProfileEntity?>()
    val profile: LiveData<ProfileEntity?> by this::_profile


    /**
     * Google Games
     */
    private suspend fun GamesSignInClient.isAuthenticatedGames() =
        runCatching { isAuthenticated.await().isAuthenticated }.getOrDefault(false)

    private suspend fun GamesSignInClient.getPlayGamesServerAuthCode() =
        runCatching { requestServerSideAccess(defaultWebClientId, false).await() }.getOrNull()

    private suspend fun GamesSignInClient.signInGames() =
        runCatching { signIn().await() }.getOrNull()


    /**
     * FirebaseAuth
     */
    private suspend fun FirebaseAuth.signInFirebaseAuth(gamesServerAuthCode: String) =
        runCatching {
            PlayGamesAuthProvider.getCredential(gamesServerAuthCode)
                .run { signInWithCredential(this) }
                .await()
        }.getOrNull()

    private suspend fun FirebaseUser.updateFirebaseUser(profileEntity: ProfileEntity) =
        updateProfile(
            userProfileChangeRequest {
                this.displayName = profileEntity.displayName
                this.photoUri = profileEntity.iconUrl?.toUri()
            }
        ).await()

    /**
     * Profile
     */
    private fun FirebaseUser.toProfile() = ProfileEntityImpl(
        uid = uid,
        displayName = displayName ?: "",
        message = "",
        iconUrl = photoUrl?.toString() ?: "",
        locale = LocaleEntityImpl(
            Locale.getDefault().language,
            Locale.getDefault().country
        )
    )

    fun requestCurrentUserProfile() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        if (gamesSignInClient.isAuthenticatedGames()) {
            _profile.value = migrationUserProfileGamesWithFirebase()
        } else {
            _profile.value = null
        }
        setProgress(false)
    }

    private suspend fun migrationUserProfileGamesWithFirebase(): ProfileEntity {
        var authCode = gamesSignInClient.getPlayGamesServerAuthCode()
//            ?: throw AuthenticationException.UnauthenticatedException("서버 인증 코드 요청에 실패 하였습니다. Games 로그인 여부를 확인해주세요.")
        if (authCode == null) {
            delay(3000)
            authCode = gamesSignInClient.getPlayGamesServerAuthCode()
                ?: throw AuthenticationException.UnauthenticatedException("서버 인증 코드 요청에 실패 하였습니다. Games 로그인 여부를 확인해주세요.")
        }

        val authResult = firebaseAuth.signInFirebaseAuth(authCode)
            ?: throw AuthenticationException.UnauthenticatedException("failed signed in firebase")

        val user = authResult.user
            ?: throw AuthenticationException.UnknownUserException("알수 없는 사용자 입니다.")

        return with(profileRepository) {
            runCatching { getProfile(user.uid) }
                .getOrElse {
                    when (it) {
                        is NullPointerException -> user.toProfile().apply { setProfile(this) }
                        else -> throw AuthenticationException.InvalidRepositoryException(it.message)
                    }
                }
                .apply { user.updateFirebaseUser(this) }
        }
    }

    sealed class AuthenticationException(message: String?) : Exception(message) {
        class UnauthenticatedException(message: String?) : AuthenticationException(message)
        class UnknownUserException(message: String?) : AuthenticationException(message)
        class InvalidRepositoryException(message: String?) : AuthenticationException(message)
    }


    fun signIn() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val result = gamesSignInClient.signInGames()
        if (result?.isAuthenticated == true) {
            _profile.value = migrationUserProfileGamesWithFirebase()
        }
        setProgress(false)
    }

    fun updateUserInfo(profileEntity: ProfileEntity) =
        viewModelScope.launch(viewModelScopeExceptionHandler) {
            val user = firebaseAuth.currentUser
                ?: throw AuthenticationException.UnknownUserException("알수 없는 사용자 입니다.")

            user.updateFirebaseUser(profileEntity)
            profileRepository.setProfile(profileEntity)
        }

    fun requestLastUserProfile() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val profile = withContext(Dispatchers.IO) {
            firebaseAuth.currentUser?.uid?.let { uid -> profileRepository.getProfile(uid) }
        }
        _profile.value = profile
        setProgress(false)
    }
}