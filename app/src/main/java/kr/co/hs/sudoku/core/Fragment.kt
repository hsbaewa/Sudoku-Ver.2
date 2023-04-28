package kr.co.hs.sudoku.core

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.extension.platform.ContextExtension.getColorCompat
import kr.co.hs.sudoku.repository.settings.GameSettingsRepository
import kr.co.hs.sudoku.viewmodel.GameSettingsViewModel
import kr.co.hs.sudoku.viewmodel.ChallengeViewModel
import kr.co.hs.sudoku.viewmodel.GamePlayViewModel
import kr.co.hs.sudoku.viewmodel.RecordViewModel
import kr.co.hs.sudoku.viewmodel.SinglePlayDifficultyViewModel

abstract class Fragment : androidx.fragment.app.Fragment() {
    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- ViewModel Provider ------------------------------------\\
    //--------------------------------------------------------------------------------------------\\

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment ViewModel Getter
     * @return StageListViewModel
     **/
    protected fun sudokuStageViewModels(): GamePlayViewModel {
        val viewModel: GamePlayViewModel by activityViewModels()
        return viewModel
    }

    protected fun singlePlayDifficultyViewModels(): SinglePlayDifficultyViewModel {
        val viewModel: SinglePlayDifficultyViewModel by activityViewModels()
        return viewModel
    }

    protected fun gameSettingsViewModels(repository: GameSettingsRepository): GameSettingsViewModel {
        val viewModel: GameSettingsViewModel
                by activityViewModels { GameSettingsViewModel.Factory(repository) }
        return viewModel
    }

    protected fun recordViewModels(): RecordViewModel {
        val viewModel: RecordViewModel by activityViewModels()
        return viewModel
    }

    protected fun challengeLeaderboardViewModels(): ChallengeViewModel {
        val viewModel: ChallengeViewModel by activityViewModels()
        return viewModel
    }

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- conv -------------------------------------------\\
    //--------------------------------------------------------------------------------------------\\

    protected val app: App by lazy { requireContext().applicationContext as App }

    companion object {
        private const val EXTRA_LEVEL = "kr.co.hs.sudoku.platform.Fragment.EXTRA_LEVEL"
        private const val EXTRA_USER_ID = "kr.co.hs.sudoku.platform.Fragment.EXTRA_USER_ID"
    }

    protected val activity: Activity
        get() = super.getActivity() as Activity

    fun Bundle.putLevel(level: Int) = putInt(EXTRA_LEVEL, level)
    protected fun getLevel() = arguments?.getInt(EXTRA_LEVEL, 0) ?: 0

    protected fun getColorCompat(colorResId: Int) = requireContext().getColorCompat(colorResId)

    fun Bundle.putUserId(uid: String) = putString(EXTRA_USER_ID, uid)
    fun getUserId() = arguments?.getString(EXTRA_USER_ID)
}