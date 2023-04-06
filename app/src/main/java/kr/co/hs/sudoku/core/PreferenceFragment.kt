package kr.co.hs.sudoku.core

import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.games.PlayGames
import com.google.firebase.auth.FirebaseAuth
import kr.co.hs.sudoku.R

abstract class PreferenceFragment : PreferenceFragmentCompat() {
    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- conv -------------------------------------------\\
    //--------------------------------------------------------------------------------------------\\

    protected val activity: Activity
        get() = super.getActivity() as Activity

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment 로그인
     **/
    protected suspend fun signIn() = createAuthenticateMediator().signIn()

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment AuthenticateMediator 생성
     * @return AuthenticateMediator
     **/
    private fun createAuthenticateMediator() = AuthenticateMediator(
        PlayGames.getGamesSignInClient(activity),
        FirebaseAuth.getInstance(),
        getString(R.string.default_web_client_id)
    )
}