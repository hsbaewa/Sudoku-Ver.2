package kr.co.hs.sudoku.feature.profile

import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import androidx.paging.liveData
import com.google.android.gms.games.GamesSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PlayGamesAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.di.google.GoogleDefaultWebClientIdQualifier
import kr.co.hs.sudoku.di.repositories.BattleRepositoryQualifier
import kr.co.hs.sudoku.di.repositories.ChallengeRepositoryQualifier
import kr.co.hs.sudoku.extension.FirebaseCloudMessagingExt.subscribeUser
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.impl.LocaleEntityImpl
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl
import kr.co.hs.sudoku.repository.battle.BattleRepository
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.viewmodel.ViewModel
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel
@Inject constructor(
    @kr.co.hs.sudoku.di.ProfileRepositoryQualifier
    val profileRepository: ProfileRepository,
    @GoogleDefaultWebClientIdQualifier
    private val defaultWebClientId: String,
    @BattleRepositoryQualifier
    private val battleRepository: BattleRepository,
    @ChallengeRepositoryQualifier
    private val challengeRepository: ChallengeRepository
) : ViewModel() {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val _profile = MutableLiveData<ProfileEntity?>()
    val profile: LiveData<ProfileEntity?> by this::_profile

    /**
     * Google Games
     */
    private suspend fun GamesSignInClient.isAuthenticatedGames() =
        runCatching {
            withContext(Dispatchers.IO) { isAuthenticated.await().isAuthenticated }
        }.getOrDefault(false)

    private suspend fun GamesSignInClient.getPlayGamesServerAuthCode() =
        runCatching {
            withContext(Dispatchers.IO) {
                requestServerSideAccess(defaultWebClientId, false).await()
            }
        }.getOrNull()

    private suspend fun GamesSignInClient.signInGames() =
        runCatching { signIn().await() }.getOrNull()


    /**
     * FirebaseAuth
     */
    private suspend fun FirebaseAuth.signInFirebaseAuth(gamesServerAuthCode: String) =
        runCatching {
            withContext(Dispatchers.IO) {
                PlayGamesAuthProvider.getCredential(gamesServerAuthCode)
                    .run { signInWithCredential(this) }
                    .await()
            }
        }.getOrNull()

    private suspend fun FirebaseUser.updateFirebaseUser(profileEntity: ProfileEntity) =
        withContext(Dispatchers.IO) {
            updateProfile(
                userProfileChangeRequest {
                    this.displayName = profileEntity.displayName
                    this.photoUri = profileEntity.iconUrl?.toUri()
                }
            ).await()
        }

    /**
     * Profile
     */
    private fun FirebaseUser.toProfile() = ProfileEntityImpl(
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

    fun requestCurrentUserProfile(gamesSignInClient: GamesSignInClient) =
        viewModelScope.launch(viewModelScopeExceptionHandler) {
            setProgress(true)

            _profile.value = firebaseAuth.currentUser
                ?.let { currentUser ->
                    with(profileRepository) {
                        runCatching {
                            withContext(Dispatchers.IO) { getProfile(currentUser.uid) }
                        }.getOrElse {
                            currentUser
                                .toProfile()
                                .apply { setProfile(this) }
                        }
                    }
                }
                ?: run {
                    if (gamesSignInClient.isAuthenticatedGames()) {
                        migrationUserProfileGamesWithFirebase(gamesSignInClient)
                    } else {
                        null
                    }
                }

            _profile.value?.run {
                FirebaseMessaging.getInstance().subscribeUser(uid).await()
            }

            setProgress(false)
        }

    private suspend fun migrationUserProfileGamesWithFirebase(gamesSignInClient: GamesSignInClient): ProfileEntity {
        var authCode = gamesSignInClient.getPlayGamesServerAuthCode()
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
            runCatching { withContext(Dispatchers.IO) { getProfile(user.uid) } }
                .getOrElse {
                    when (it) {
                        is NullPointerException -> user.toProfile().apply {
                            withContext(Dispatchers.IO) { setProfile(this@apply) }
                        }

                        else -> throw AuthenticationException.InvalidRepositoryException(it.message)
                    }
                }
                .apply {
                    checkIn(this)
                    user.updateFirebaseUser(this)
                }
        }
    }

    sealed class AuthenticationException(message: String?) : Exception(message) {
        class UnauthenticatedException(message: String?) : AuthenticationException(message)
        class UnknownUserException(message: String?) : AuthenticationException(message)
        class InvalidRepositoryException(message: String?) : AuthenticationException(message)
    }


    fun signIn(gamesSignInClient: GamesSignInClient) =
        viewModelScope.launch(viewModelScopeExceptionHandler) {
            setProgress(true)
            val result = gamesSignInClient.signInGames()
            if (result?.isAuthenticated == true) {
                _profile.value = migrationUserProfileGamesWithFirebase(gamesSignInClient)
            }
            setProgress(false)
        }

    fun updateUserInfo(onComplete: (Boolean) -> Unit) =
        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            viewModelScopeExceptionHandler.handleException(coroutineContext, throwable)
            onComplete(false)
        }) {
            setProgress(true)
            val user = firebaseAuth.currentUser
                ?: throw AuthenticationException.UnknownUserException("알수 없는 사용자 입니다.")

            profile.value?.run {
                user.updateFirebaseUser(this)
                withContext(Dispatchers.IO) { profileRepository.setProfile(this@run) }
            }
            setProgress(false)
            onComplete(true)
        }

    fun requestLastUserProfile() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val profile = withContext(Dispatchers.IO) {
            firebaseAuth.currentUser?.uid?.let { uid -> profileRepository.getProfile(uid) }
        }
        _profile.value = profile
        setProgress(false)
    }

    fun setDisplayName(displayName: String) {
        _profile.value?.displayName = displayName
    }

    fun setMessage(message: String) {
        _profile.value?.message = message
    }

    fun setIconUrl(iconUrl: String) {
        _profile.value?.iconUrl = iconUrl
    }

    inline fun requestProfile(
        uid: String,
        crossinline onStatus: (RequestStatus<ProfileEntity>) -> Unit
    ) = viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
        onStatus(OnError(throwable))
    }) {
        onStatus(OnStart())
        val entity = withContext(Dispatchers.IO) { profileRepository.getProfile(uid) }
        onStatus(OnFinish(entity))
    }

    fun requestProfile(uid: String) = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val profile = withContext(Dispatchers.IO) { profileRepository.getProfile(uid) }
        _profile.value = profile
        setProgress(false)
    }

    fun getProfilePagingData(
        uid: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ): LiveData<PagingData<ProfileItem>> {
        return Pager(
            config = PagingConfig(pageSize = 10, initialLoadSize = 10),
            pagingSourceFactory = {
                ProfilePagingSource(
                    uid,
                    profileRepository,
                    battleRepository,
                    challengeRepository
                )
            }
        ).liveData.cachedIn(viewModelScope)
    }

    private class ProfilePagingSource(
        private val uid: String,
        private val profileRepository: ProfileRepository,
        private val battleRepository: BattleRepository,
        private val challengeRepository: ChallengeRepository,
    ) : PagingSource<Long, ProfileItem>() {
        override fun getRefreshKey(state: PagingState<Long, ProfileItem>) = null
        override suspend fun load(params: LoadParams<Long>): LoadResult<Long, ProfileItem> {
            val isFirst = params.key == null

            val profileItems = buildList {
                if (isFirst) {
                    val profile =
                        withContext(Dispatchers.IO) { profileRepository.getProfile(uid) }
                    profile.iconUrl?.let { url -> add(ProfileItem.Icon(url)) }
                    add(
                        ProfileItem.DisplayName(
                            profile.displayName,
                            profile.locale?.getLocaleFlag()
                        )
                    )
                    profile.message.takeUnless { it.isNullOrEmpty() }
                        ?.let { message -> add(ProfileItem.Message(message)) }

                    val lastCheckedAt = when (profile) {
                        is ProfileEntity.OnlineUserEntity -> profile.checkedAt
                        is ProfileEntity.UserEntity -> profile.lastCheckedAt
                    }
                    lastCheckedAt?.let { checkedAt -> add(ProfileItem.LastChecked(checkedAt)) }


                    val ladder =
                        withContext(Dispatchers.IO) { battleRepository.getLeaderBoard(uid) }

                    add(
                        ProfileItem.BattleLadder(
                            ladder.playCount,
                            ladder.winCount,
                            ladder.ranking
                        )
                    )
                }
            }


            val count = params.loadSize.toLong()
            val challengeHistory =
                withContext(Dispatchers.IO) {
                    params.key
                        ?.run { challengeRepository.getHistory(uid, Date(this), count) }
                        ?: run { challengeRepository.getHistory(uid, count) }
                }.map { ProfileItem.ChallengeLog(it) }
            val nextKey =
                challengeHistory.takeIf { it.isNotEmpty() }?.lastOrNull()?.item?.clearAt?.time

            return LoadResult.Page(
                profileItems.toMutableList().apply {
                    if (challengeHistory.isNotEmpty()) {
                        add(ProfileItem.Divider(ProfileItem.DividerType.Challenge))
                        addAll(challengeHistory)
                    }
                },
                null,
                nextKey
            )
        }
    }
}