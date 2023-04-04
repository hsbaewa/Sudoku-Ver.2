package kr.co.hs.sudoku

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseUser
import kr.co.hs.sudoku.databinding.ActivityAuthTestBinding

class AuthTestActivity : FirebaseAuthActivity() {

    lateinit var binding: ActivityAuthTestBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth_test)
        signInWithGames()
    }

    override fun onSignInFirebaseAuth(user: FirebaseUser) {
        binding.tvInfo.text = user.displayName
    }

    override fun onSignInFirebaseError(t: Throwable) {
        throw t
    }
}