package kr.co.hs.sudoku.feature.profile

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.di.repositories.BattleRepositoryQualifier
import kr.co.hs.sudoku.di.repositories.ChallengeRepositoryQualifier
import kr.co.hs.sudoku.extension.FirebaseCloudMessagingExt.subscribeUser
import kr.co.hs.sudoku.feature.user.Authenticator
import kr.co.hs.sudoku.feature.user.GoogleGamesAuthenticator
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.battle.BattleRepository
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.viewmodel.ViewModel
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel
@Inject constructor(
    @BattleRepositoryQualifier
    private val battleRepository: BattleRepository,
    @ChallengeRepositoryQualifier
    private val challengeRepository: ChallengeRepository
) : ViewModel() {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firebaseMessaging: FirebaseMessaging by lazy { FirebaseMessaging.getInstance() }

    private val _profile = MutableLiveData<ProfileEntity?>()
    val profile: LiveData<ProfileEntity?> by this::_profile

    fun requestCurrentUserProfile(authenticator: Authenticator) {
        setProgress(true)
        viewModelScope.launch {
            authenticator.getProfile()
                .catch {
                    setProgress(false)
                    when (it) {
                        is Authenticator.RequireSignIn -> _profile.value = null
                        else -> setError(it)
                    }
                }
                .collect {
                    setProgress(false)
                    _profile.value = it
                    firebaseMessaging.subscribeUser(it.uid).await()
                }
        }
    }

    fun signIn(authenticator: Authenticator) {
        setProgress(true)
        viewModelScope.launch {
            authenticator.signIn()
                .catch {
                    setProgress(false)
                    when (it) {
                        is GoogleGamesAuthenticator.SignInFailed -> _profile.value = null
                        else -> setError(it)
                    }
                }
                .collect {
                    setProgress(false)
                    _profile.value = it
                    firebaseMessaging.subscribeUser(it.uid).await()
                }
        }
    }

    fun updateUserInfo(authenticator: Authenticator, onComplete: (Boolean) -> Unit) {
        val profile = profile.value ?: throw Exception("알수 없는 사용자 입니다.")

        setProgress(true)
        viewModelScope.launch {
            authenticator.updateProfile(profile)
                .catch {
                    setProgress(false)
                    setError(it)
                }
                .collect {
                    setProgress(false)
                    onComplete(true)
                }
        }
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


    fun getProfilePagingData(
        authenticator: Authenticator,
        uid: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ): LiveData<PagingData<ProfileItem>> {
        return Pager(
            config = PagingConfig(pageSize = 10, initialLoadSize = 10),
            pagingSourceFactory = {
                ProfilePagingSource(
                    uid,
                    authenticator,
                    battleRepository,
                    challengeRepository
                )
            }
        ).liveData.cachedIn(viewModelScope)
    }

    private class ProfilePagingSource(
        private val uid: String,
        private val authenticator: Authenticator,
        private val battleRepository: BattleRepository,
        private val challengeRepository: ChallengeRepository,
    ) : PagingSource<Long, ProfileItem>() {
        override fun getRefreshKey(state: PagingState<Long, ProfileItem>) = null
        override suspend fun load(params: LoadParams<Long>): LoadResult<Long, ProfileItem> {
            val isFirst = params.key == null

            val profileItems = buildList {
                if (isFirst) {
                    val profile = authenticator.getProfile(uid)
                        .catch { }
                        .firstOrNull()

                    profile?.iconUrl?.let { url -> add(ProfileItem.Icon(url)) }
                    add(
                        ProfileItem.DisplayName(
                            profile?.displayName ?: "",
                            profile?.locale?.getLocaleFlag()
                        )
                    )
                    profile?.message.takeUnless { it.isNullOrEmpty() }
                        ?.let { message -> add(ProfileItem.Message(message)) }

                    val lastCheckedAt = when (profile) {
                        is ProfileEntity.OnlineUserEntity -> profile.checkedAt
                        is ProfileEntity.UserEntity -> profile.lastCheckedAt
                        else -> Date()
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