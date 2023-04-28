package kr.co.hs.sudoku.feature.settings

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.SwitchPreferenceCompat
import com.google.android.gms.games.PlayGames
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.auth.FirebaseAuthMediatorImpl
import kr.co.hs.sudoku.core.PreferenceFragment
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dataStore
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.getDrawable
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showSnackBar
import kr.co.hs.sudoku.feature.profile.ProfileDialog
import kr.co.hs.sudoku.model.settings.GameSettingsEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.GameSettingsRepositoryImpl
import kr.co.hs.sudoku.views.ProfilePreference
import java.net.URL

class SettingsFragment : PreferenceFragment() {

    companion object {
        fun new() = SettingsFragment()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) =
        addPreferencesFromResource(R.xml.preferences_settings)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch(coroutineExceptionHandler) {
            // 화면 진입 시 마다 currentUser 체크 하여 표시
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                showProgressIndicator()
                val profileEntity = withContext(Dispatchers.IO) {
                    currentUser.takeIf { it != null }?.run { getProfile() }
                }
                profileEntity?.run { onChangedProfile(this) }
                dismissProgressIndicator()
            }
        }

        findSignInPreference()?.onPreferenceClickListener = clickSignInCallback
        findEditProfilePreference()?.onPreferenceClickListener = clickEditProfileCallback
        findHapticFeedbackPreference()?.onPreferenceClickListener = clickSettingsHapticFeedback

        // 게임 설정 관련 변경시 적용 하기 위한 observer
        gameSettingsViewModel.gameSettings.observe(viewLifecycleOwner, gameSettingsChangedObserver)
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, t ->
        dismissProgressIndicator()
        showSnackBar(t.message.toString())
    }

    private suspend fun FirebaseUser.getProfile() = createFirebaseAuthMediator().run {
        runCatching { getProfile(uid) }
            .onFailure {
                if (it is NullPointerException) {
                    // 프로필 이 없는 경우 디폴트 값으로 채워 준다.
                    val profile = toDomain()
                    updateProfile(profile)
                    onChangedProfile(profile)
                }
            }
            .getOrNull()
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/15
     * @comment 현재 로그인 된 firebase user
     * @return FirebaseUser
     **/
    private val currentUser = FirebaseAuth.getInstance().currentUser

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/16
     * @comment 로그인 된 사용자 profile 이 변경 되었을 때 호출 됨
     * @param profileEntity
     **/
    private fun onChangedProfile(profileEntity: ProfileEntity?) {
        findSignInPreference()?.setupUISignInPreference(profileEntity)
        findEditProfilePreference()?.setupUIEditProfile(profileEntity)
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 로그인 Preference 찾기
     * @return Preference for sign in
     **/
    private fun findSignInPreference() =
        findPreference<ProfilePreference>(getString(R.string.preferences_key_sign_in))

    private fun ProfilePreference.setupUISignInPreference(profileEntity: ProfileEntity?) {
        profileEntity?.let {
            title = it.displayName
            summary = it.message
            val errorDrawable = ContextCompat.getDrawable(context, R.drawable.games_controller)
            loadIcon(URL(it.iconUrl), errorDrawable)
        } ?: kotlin.run {
            title = getString(R.string.preferences_sign_in)
            summary = null
            icon = getDrawable(R.drawable.games_controller)
        }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/14
     * @comment 프로필 수정 설정 버튼
     * @return 프로필 수정 Preference
     **/
    private fun findEditProfilePreference() =
        findPreference<Preference>(getString(R.string.preferences_key_profile))

    private fun Preference.setupUIEditProfile(profileEntity: ProfileEntity?) {
        isVisible = profileEntity != null
    }

    private val clickSignInCallback = OnPreferenceClickListener {
        viewLifecycleOwner.lifecycleScope.launch(coroutineExceptionHandler) {
            showProgressIndicator()
            with(createFirebaseAuthMediator()) {
                withContext(Dispatchers.IO) {
                    signIn()
                }
                    ?.run { getProfile() }
                    ?.run { onChangedProfile(this) }
            }

            dismissProgressIndicator()
        }
        return@OnPreferenceClickListener true
    }


    private val clickEditProfileCallback = OnPreferenceClickListener {
        viewLifecycleOwner.lifecycleScope.launch {

            val profile = withContext(Dispatchers.IO) {
                currentUser?.getProfile()
            }
            profile?.let {
                val onSubmit = { resultProfile: ProfileEntity ->
                    updateProfile(resultProfile)
                }
                ProfileDialog(requireContext(), it, true, onSubmit).show()
            }
        }
        return@OnPreferenceClickListener true
    }

    private fun updateProfile(profileEntity: ProfileEntity) {
        viewLifecycleOwner.lifecycleScope.launch(coroutineExceptionHandler) {
            showProgressIndicator()

            onChangedProfile(withContext(Dispatchers.IO) {
                with(createFirebaseAuthMediator()) { updateProfile(profileEntity) }
            })

            dismissProgressIndicator()
        }
    }

    private fun findHapticFeedbackPreference() =
        findPreference<SwitchPreferenceCompat>(getString(R.string.preferences_key_enabled_haptic_feedback))

    private val clickSettingsHapticFeedback = OnPreferenceClickListener {
        val enabled = (it as SwitchPreferenceCompat).isChecked
        gameSettingsViewModel.setGameSettings(GameSettingsEntity(enabledHapticFeedback = enabled))
        return@OnPreferenceClickListener true
    }

    private val gameSettingsViewModel
            by lazy { gameSettingsViewModels(GameSettingsRepositoryImpl(dataStore)) }

    private val gameSettingsChangedObserver: Observer<GameSettingsEntity> =
        Observer { findHapticFeedbackPreference()?.isChecked = it.enabledHapticFeedback }

    private fun createFirebaseAuthMediator() = FirebaseAuthMediatorImpl(
        FirebaseAuth.getInstance(),
        PlayGames.getGamesSignInClient(activity),
        getString(R.string.default_web_client_id)
    )
}