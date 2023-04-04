package kr.co.hs.sudoku

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PlayGamesAuthProvider

abstract class FirebaseAuthActivity : GoogleGamesSignInActivity() {

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment Games를 통해 FirebaseAuth 로그인
     **/
    protected fun signInWithGames() = signInGames()

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 로그인 결과 콜백
     * @param isAuthenticated 인증 성공 여부
     **/
    override fun onSignInGamesSuccess(isAuthenticated: Boolean) {
        if (isAuthenticated) {
            getServerAuthToken()
                .addOnSuccessListener { signInWithCredential(PlayGamesAuthProvider.getCredential(it)) }
                .addOnFailureListener { onSignInGamesError(it) }
        }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 인증 실패시 호출되는 함수
     * @param t 예외
     **/
    override fun onSignInGamesError(t: Throwable) = onSignInFirebaseError(t)


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment credential 을 이용한 로그인
     * @param credential
     **/
    private fun signInWithCredential(credential: AuthCredential) {
        FirebaseAuth.getInstance()
            .signInWithCredential(credential)
            .addOnSuccessListener { onSignInFirebaseAuth(it.user!!) }
            .addOnFailureListener { onSignInFirebaseError(it) }
    }

    abstract fun onSignInFirebaseAuth(user: FirebaseUser)
    abstract fun onSignInFirebaseError(t: Throwable)
}