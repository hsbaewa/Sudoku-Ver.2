package kr.co.hs.sudoku.feature.settings

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.extension.CoilExt.loadIcon
import kr.co.hs.sudoku.extension.firebase.FirebaseAuthExt.getCurrentUser
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showSnackBar
import kr.co.hs.sudoku.core.PreferenceFragment
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dataStore
import kr.co.hs.sudoku.model.settings.GameSettingsEntity
import kr.co.hs.sudoku.repository.GameSettingsRepositoryImpl

class SettingsFragment : PreferenceFragment() {

    companion object {
        fun new() = SettingsFragment()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) =
        addPreferencesFromResource(R.xml.preferences_settings)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {

            // 화면 진입 시 마다 currentUser 체크하여 표시
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                getCurrentUser()?.run { onSignIn(this) }
            }
        }

        gameSettingsViewModel.gameSettings.observe(viewLifecycleOwner) {
            findEnabledHapticFeedbackPreference()?.isChecked = it.enabledHapticFeedback
        }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment 로그인 된 사용자 표시
     **/
    fun onSignIn(user: FirebaseUser) = findSignInPreference()?.setupUISignIn(user)

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
        loadIcon(user.photoUrl) {
            crossfade(true)
            transformations(CircleCropTransformation())
        }
    }

    private val gameSettingsViewModel by lazy {
        gameSettingsViewModels(GameSettingsRepositoryImpl(dataStore))
    }

    private fun findEnabledHapticFeedbackPreference() =
        findPreference<SwitchPreferenceCompat>(getString(R.string.preferences_key_enabled_haptic_feedback))

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment 설정 아이템 클릭 이벤트
     **/
    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        return when (preference.key) {
            getString(R.string.preferences_key_sign_in) -> onClickSignIn()
            getString(R.string.preferences_key_enabled_haptic_feedback) -> {
                val enabled = (preference as SwitchPreferenceCompat).isChecked
                onClickEnableHapticFeedback(enabled)
            }
            else -> super.onPreferenceTreeClick(preference)
        }
    }

    private fun onClickSignIn(): Boolean {
        viewLifecycleOwner.lifecycleScope.launch(CoroutineExceptionHandler { _, t ->
            showSnackBar(t.message.toString())
            dismissProgressIndicator()
        }
        ) {
            showProgressIndicator()
            val auth = withContext(Dispatchers.IO) { signIn() }
            auth?.user?.run { onSignIn(this) }
            dismissProgressIndicator()
        }
        return true
    }

    private fun onClickEnableHapticFeedback(on: Boolean): Boolean {
        gameSettingsViewModel.setGameSettings(GameSettingsEntity(enabledHapticFeedback = on))
        return true
    }
}