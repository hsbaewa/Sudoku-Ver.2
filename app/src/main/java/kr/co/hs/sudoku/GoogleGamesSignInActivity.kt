package kr.co.hs.sudoku

import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.games.PlayGames
import kotlinx.coroutines.tasks.await

abstract class GoogleGamesSignInActivity : AppCompatActivity() {

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment Games 로그인
     * @return Task<AuthenticationResult>
     **/
    protected fun signInGames() = PlayGames
        .getGamesSignInClient(this)
        .signIn()
        .addOnSuccessListener { onSignInGamesSuccess(it.isAuthenticated) }
        .addOnFailureListener { onSignInGamesError(it) }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment Games로그인 성공 시에 호출
     * @param isAuthenticated 인증 성공 여부
     **/
    abstract fun onSignInGamesSuccess(isAuthenticated: Boolean)

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 서버측 액세스(https://developer.android.com/games/pgs/android/server-access?hl=ko)로 Firebase Auth와 연동시 필요
     * @return ServerAuthToken String
     **/
    protected fun getServerAuthToken() =
        PlayGames.getGamesSignInClient(this)
            .requestServerSideAccess(getString(R.string.default_web_client_id), false)

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment Games 로그인 실패 시 호출
     * @param t 예외
     **/
    abstract fun onSignInGamesError(t: Throwable)

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment Games에 로그인 된 상태인지?
     * @return Boolean
     **/
    protected suspend fun isAuthenticated() = PlayGames
        .getGamesSignInClient(this)
        .isAuthenticated
        .await()
        .isAuthenticated

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment 현재 로그인된 player 정보
     * @return Player 정보
     **/
    protected suspend fun getPlayer() = PlayGames
        .getPlayersClient(this)
        .currentPlayer
        .await()
}