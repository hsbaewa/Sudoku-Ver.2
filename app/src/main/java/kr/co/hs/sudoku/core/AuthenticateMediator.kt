package kr.co.hs.sudoku.core

import com.google.android.gms.games.GamesSignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PlayGamesAuthProvider
import kotlinx.coroutines.tasks.await

class AuthenticateMediator(
    private val gamesSignInClient: GamesSignInClient,
    private val firebaseAuth: FirebaseAuth,
    private val clientId: String
) {

    suspend fun sync() =
        takeIf { isAuthenticatedGames() && !hasCurrentFirebaseUser() }
            ?.run { getPlayGamesServerAuthCode() }
            ?.let { PlayGamesAuthProvider.getCredential(it) }
            ?.run { signInWithCredential(this) }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment Play Games 로부터 인증된 사용자인지 확인
     * @return 인증 여부
     **/
    private suspend fun isAuthenticatedGames() =
        gamesSignInClient
            .isAuthenticated
            .await()
            .isAuthenticated

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment Firebase Auth 에 로그인 되어 있는지 여부 currentUser를 가지고 있는지 여부
     * @return Firebase Auth 인증 여부
     **/
    private fun hasCurrentFirebaseUser() = firebaseAuth.currentUser != null

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment 일반적인 ID / PW 가 아닌 Credential을 이용한 로그인
     * @param credential 제공자로 부터 받은 Credential
     **/
    private suspend fun signInWithCredential(credential: AuthCredential) =
        firebaseAuth.signInWithCredential(credential).await()

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment PlayGames로부터 서버 인증 코드 획득
     * @param clientId
     * @return String 형태의 서버 인증 코드
     **/
    private suspend fun getPlayGamesServerAuthCode() =
        gamesSignInClient.requestServerSideAccess(clientId, false).await()


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment 통합 로그인, Games 로그인 안되어 있는경우 로그인 시도하고 성공 후에 Firebase 인증
     **/
    suspend fun signIn(): AuthResult? {
        takeUnless { isAuthenticatedGames() }
            ?.run { gamesSignInClient.signIn().await() }
            ?.isAuthenticated
            ?.takeUnless { it }
            ?.run { throw Exception("sign in failed") }

        return getPlayGamesServerAuthCode()
            .takeIf { it != null }
            ?.let { PlayGamesAuthProvider.getCredential(it) }
            ?.let { signInWithCredential(it) }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment 통합 로그인 여부, Games와 Firebase 모두 로그인 되어 있는지 확인
     * @return Boolean
     **/
    suspend fun isAuthenticated() =
        isAuthenticatedGames() && hasCurrentFirebaseUser()
}