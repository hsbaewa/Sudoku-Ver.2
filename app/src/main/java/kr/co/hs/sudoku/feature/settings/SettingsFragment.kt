package kr.co.hs.sudoku.feature.settings

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.SwitchPreferenceCompat
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.PreferenceFragment
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dataStore
import kr.co.hs.sudoku.extension.platform.FragmentExtension.getDrawable
import kr.co.hs.sudoku.feature.UserProfileViewModel
import kr.co.hs.sudoku.feature.profile.ProfileDialog
import kr.co.hs.sudoku.model.settings.GameSettingsEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.GameSettingsRepositoryImpl
import kr.co.hs.sudoku.viewmodel.GameSettingsViewModel
import kr.co.hs.sudoku.views.ProfilePreference
import java.net.URL

class SettingsFragment : PreferenceFragment(), OnPreferenceClickListener {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    private val userInfoViewModel: UserProfileViewModel by activityViewModels()
    private val gameSettingsViewModel: GameSettingsViewModel by viewModels {
        GameSettingsViewModel.Factory(GameSettingsRepositoryImpl(dataStore))
    }
    private val preferenceSignIn: ProfilePreference by lazy {
        findPreference(getString(R.string.preferences_key_sign_in))!!
    }
    private val preferenceEditProfile: Preference by lazy {
        findPreference(getString(R.string.preferences_key_profile))!!
    }
    private val preferenceHapticFeedback: SwitchPreferenceCompat by lazy {
        findPreference(getString(R.string.preferences_key_enabled_haptic_feedback))!!
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) =
        addPreferencesFromResource(R.xml.preferences_settings)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceSignIn.onPreferenceClickListener = this
        preferenceEditProfile.onPreferenceClickListener = this
        preferenceHapticFeedback.onPreferenceClickListener = this

        // 게임 설정 관련 변경시 적용 하기 위한 observer
        gameSettingsViewModel.gameSettings.observe(viewLifecycleOwner) {
            preferenceHapticFeedback.isChecked = it.enabledHapticFeedback
        }

        userInfoViewModel.profile.observe(viewLifecycleOwner) {
            preferenceSignIn.setupUISignInPreference(it)
            preferenceEditProfile.setupUIEditProfile(it)
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        return when (preference.key) {
            getString(R.string.preferences_key_sign_in) -> {
                userInfoViewModel.signIn()
                true
            }

            getString(R.string.preferences_key_profile) -> {
                with(userInfoViewModel) {
                    profile.value
                        ?.run {
                            ProfileDialog(requireContext(), this, true) {
                                updateUserInfo(it)
                            }
                        }
                        ?.show()
                }
                true
            }

            getString(R.string.preferences_key_enabled_haptic_feedback) -> {
                val enabled = (preference as SwitchPreferenceCompat).isChecked
                gameSettingsViewModel.setGameSettings(GameSettingsEntity(enabledHapticFeedback = enabled))
                true
            }

            else -> false
        }
    }

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

    private fun Preference.setupUIEditProfile(profileEntity: ProfileEntity?) {
        isVisible = profileEntity != null
    }
}