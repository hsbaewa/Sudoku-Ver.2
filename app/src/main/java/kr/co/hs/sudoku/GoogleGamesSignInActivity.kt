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
    protected fun doSignInGames() = PlayGames
        .getGamesSignInClient(this)
        .signIn()

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