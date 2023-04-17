package kr.co.hs.sudoku.auth

import androidx.core.net.toUri
import com.google.android.gms.games.GamesSignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PlayGamesAuthProvider
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.di.UseCase
import kr.co.hs.sudoku.di.UseCase.setProfile
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.impl.LocaleEntityImpl
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl
import java.util.*

class FirebaseAuthMediatorImpl(
    private val firebaseAuth: FirebaseAuth,
    private val gamesSignInClient: GamesSignInClient,
    private val clientId: String
) : FirebaseAuthMediator {

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment 통합 로그인, Games 로그인 안되어 있는 경우 로그인 시도 하고 성공 후에 Firebase 인증
     **/
    override suspend fun signIn(): FirebaseUser? {
        takeUnless { isAuthenticatedGames() }
            ?.run { signInPlayGames() }
        return migrationWithPlayGames()
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment Play Games 로부터 인증된 사용자인지 확인
     * @return 인증 여부
     **/
    private suspend fun isAuthenticatedGames() = gamesSignInClient
        .isAuthenticated
        .await()
        .isAuthenticated

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/17
     * @comment Play Games에 로그인
     **/
    private suspend fun signInPlayGames() = gamesSignInClient
        .signIn()
        .await()
        ?.isAuthenticated
        ?.takeUnless { it }
        ?.run { throw Exception("play game sign in failed") }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/17
     * @comment PlayGames의 서버 인증 코드로 firebase auth에 로그인
     * @return
     **/
    suspend fun migrationWithPlayGames() = getPlayGamesServerAuthCode()
        ?.let { PlayGamesAuthProvider.getCredential(it) }
        ?.run { signInWithCredential(this) }
        ?.user

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment PlayGames로부터 서버 인증 코드 획득
     * @return String 형태의 서버 인증 코드
     **/
    private suspend fun getPlayGamesServerAuthCode() =
        gamesSignInClient.requestServerSideAccess(clientId, false).await()

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment 일반적인 ID / PW 가 아닌 Credential을 이용한 로그인
     * @param credential 제공자로 부터 받은 Credential
     **/
    private suspend fun signInWithCredential(credential: AuthCredential) =
        firebaseAuth.signInWithCredential(credential).await()

    override suspend fun getProfile(uid: String) = UseCase.getProfile(uid).last()

    override suspend fun updateProfile(profileEntity: ProfileEntity) = firebaseAuth.currentUser
        ?.run {
            updateFirebaseUser(profileEntity)
            setProfile(profileEntity).collect()
            profileEntity
        }

    private suspend fun FirebaseUser.updateFirebaseUser(profileEntity: ProfileEntity) =
        updateProfile(userProfileChangeRequest {
            this.displayName = profileEntity.displayName
            this.photoUri = profileEntity.iconUrl.toUri()
        }).await()

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/17
     * @comment Play Games와 마이그레이션이 필요한가?
     * @return 필요 여부
     **/
    suspend fun needMigrationWithPlayGames() =
        isAuthenticatedGames() && firebaseAuth.currentUser == null


    fun FirebaseUser.toDomain() = ProfileEntityImpl(
        uid = uid,
        displayName = displayName ?: "",
        message = "",
        iconUrl = this.photoUrl?.toString() ?: "",
        locale = LocaleEntityImpl(Locale.getDefault().language, Locale.getDefault().country)
    )
}