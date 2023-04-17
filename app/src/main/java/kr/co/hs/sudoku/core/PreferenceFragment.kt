package kr.co.hs.sudoku.core

import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceFragmentCompat
import kr.co.hs.sudoku.repository.settings.GameSettingsRepository
import kr.co.hs.sudoku.viewmodel.GameSettingsViewModel

abstract class PreferenceFragment : PreferenceFragmentCompat() {
    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- ViewModelProvider -------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    protected fun gameSettingsViewModels(repository: GameSettingsRepository): GameSettingsViewModel {
        val viewModel: GameSettingsViewModel
                by activityViewModels { GameSettingsViewModel.Factory(repository) }
        return viewModel
    }

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- conv -------------------------------------------\\
    //--------------------------------------------------------------------------------------------\\

    protected val activity: Activity
        get() = super.getActivity() as Activity
}