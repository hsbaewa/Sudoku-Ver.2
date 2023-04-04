package kr.co.hs.sudoku

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import coil.Coil
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.view.MainActivity

class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_settings)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {

            // 화면 진입 시 마다 currentUser 체크하여 표시
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                FirebaseAuth.getInstance().currentUser?.run {
                    findSignInPreference()?.setupUISignIn(this)
                }
            }

        }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 로그인 Preference 찾기
     * @return Preference for signin
     **/
    private fun findSignInPreference() =
        findPreference<Preference>(getString(R.string.preferences_key_sign_in))


    private fun Preference.setupUISignIn(user: FirebaseUser) {
        title = user.displayName
        viewLifecycleOwner.lifecycleScope.launch(CoroutineExceptionHandler { _, _ ->
            icon = null
        }) {
            icon = null
            icon = withContext(Dispatchers.IO) {
                user.doGetPhoto()
            }
        }
    }

    private suspend fun FirebaseUser.doGetPhoto() = context
        .takeIf { it != null }
        ?.run {
            ImageRequest.Builder(this)
                .data(photoUrl)
                .build()
        }
        ?.run {
            Coil.imageLoader(context).execute(this).drawable
        }


    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        return when (preference.key) {
            getString(R.string.preferences_key_sign_in) -> onClickSignIn()
            else -> super.onPreferenceTreeClick(preference)
        }
    }

    private fun onClickSignIn(): Boolean {
        viewLifecycleOwner.lifecycleScope.launch {
            val authResult = (activity as MainActivity).doSignInGames()
            authResult?.user?.run { onSignIn(this) }
        }
        return true
    }

    fun onSignIn(user: FirebaseUser) {
        findSignInPreference()?.setupUISignIn(user)
    }
}