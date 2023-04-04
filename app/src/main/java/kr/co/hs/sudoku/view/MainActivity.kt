package kr.co.hs.sudoku.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.games.PlayGames
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PlayGamesAuthProvider
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.SettingsFragment
import kr.co.hs.sudoku.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        replaceTabFragment(SelectStageFragment.newInstance())
        binding.navigationBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.selectStage -> replaceTabFragment(SelectStageFragment.newInstance())
                R.id.settings -> replaceTabFragment(SettingsFragment.newInstance())
            }
            return@setOnItemSelectedListener true
        }

        doMigrateAuth()
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment Content Fragment 교체
     * @param fragment 교체할 Fragment
     **/
    private fun replaceTabFragment(fragment: Fragment) =
        with(supportFragmentManager.beginTransaction()) {
            replace(R.id.tabContentLayout, fragment, fragment::class.java.simpleName)
            commit()
        }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment Games 계정과 Firebase 계정 마이그레이션, Games 계정이 로그인 되어 있는 경우 해당 Credential로 Firebase에 로그인 시킨다
     **/
    private fun doMigrateAuth() = lifecycleScope.launch(CoroutineExceptionHandler { _, t ->
        showMessage(t.message.toString())
        hideProgress()
    }) {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (isAuthenticatedGames() && !hasFirebaseUser()) {
                val authResult = signInFirebaseForGamesCredential()
                findSettingsFragment()?.onSignIn(authResult.user!!)
            }

            hideProgress()
        }
    }

    private fun showMessage(message: String) =
        Snackbar.make(window.decorView.rootView, message, Snackbar.LENGTH_SHORT).show()

    private fun hideProgress() = binding.progress.hide()

    private suspend fun isAuthenticatedGames() =
        PlayGames.getGamesSignInClient(this)
            .isAuthenticated
            .await()
            .isAuthenticated

    private fun hasFirebaseUser() = FirebaseAuth.getInstance().currentUser != null

    private suspend fun signInFirebaseForGamesCredential() =
        FirebaseAuth.getInstance()
            .signInWithCredential(getPlayGamesCredential())
            .await()

    private suspend fun getPlayGamesCredential() =
        PlayGamesAuthProvider.getCredential(
            PlayGames.getGamesSignInClient(this)
                .requestServerSideAccess(getString(R.string.default_web_client_id), false)
                .await()
        )

    private fun findSettingsFragment() = with(supportFragmentManager) {
        findFragmentByTag(SettingsFragment::class.java.simpleName) as? SettingsFragment
    }

    private suspend fun signInGames() =
        PlayGames.getGamesSignInClient(this)
            .signIn()
            .await()

    suspend fun doSignInGames(): AuthResult? {
        showProgress()
        val authResult = withContext(Dispatchers.IO) {
            if (signInGames().isAuthenticated) {
                signInFirebaseForGamesCredential()
            } else null
        }
        hideProgress()
        return authResult
    }

    private fun showProgress() = binding.progress.show()
}