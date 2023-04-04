package kr.co.hs.sudoku

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_settings)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        return when (preference.key) {
            getString(R.string.preferences_key_sign_in) -> onClickSignIn()
            else -> super.onPreferenceTreeClick(preference)
        }
    }

    private fun onClickSignIn(): Boolean {
        // TODO : 로그인
        return true
    }
}