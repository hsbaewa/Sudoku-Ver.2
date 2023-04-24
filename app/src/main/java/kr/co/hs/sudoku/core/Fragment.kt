package kr.co.hs.sudoku.core

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import kr.co.hs.sudoku.di.Repositories
import kr.co.hs.sudoku.repository.settings.GameSettingsRepository
import kr.co.hs.sudoku.viewmodel.GameSettingsViewModel
import kr.co.hs.sudoku.viewmodel.RankingViewModel
import kr.co.hs.sudoku.viewmodel.SudokuStatusViewModel
import kr.co.hs.sudoku.viewmodel.SudokuStageViewModel
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

    protected fun challengeRankingViewModels(): RankingViewModel {
        val factory = RankingViewModel.Factory(
            Repositories.getChallengeRankingRepository(),
            FirebaseAuth.getInstance().currentUser?.uid ?: ""
        )
        val viewModel: RankingViewModel by activityViewModels { factory }
        return viewModel
    }

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- conv -------------------------------------------\\
    //--------------------------------------------------------------------------------------------\\

    companion object {
        private const val EXTRA_LEVEL = "kr.co.hs.sudoku.platform.Fragment.EXTRA_LEVEL"
    }

    protected val activity: Activity
        get() = super.getActivity() as Activity

    fun Bundle.putLevel(level: Int) = putInt(EXTRA_LEVEL, level)
    protected fun getLevel() = arguments?.getInt(EXTRA_LEVEL, 0) ?: 0
}