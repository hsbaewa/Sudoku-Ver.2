package kr.co.hs.sudoku.core

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.activityViewModels
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.extension.platform.ContextExtension.getColorCompat
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.parcel.MatrixParcelModel
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

    protected val activity: Activity
        get() = super.getActivity() as Activity

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- Extra data ----------------------------------------\\
    //--------------------------------------------------------------------------------------------\\

    companion object {
        private const val EXTRA_LEVEL = "kr.co.hs.sudoku.platform.Fragment.EXTRA_LEVEL"
        private const val EXTRA_USER_ID = "kr.co.hs.sudoku.platform.Fragment.EXTRA_USER_ID"
        private const val EXTRA_MATRIX = "kr.co.hs.sudoku.platform.Fragment.EXTRA_MATRIX"
    }


    fun Bundle.putLevel(level: Int) = putInt(EXTRA_LEVEL, level)
    protected fun getLevel() = arguments?.getInt(EXTRA_LEVEL, 0) ?: 0

    protected fun getColorCompat(colorResId: Int) = requireContext().getColorCompat(colorResId)

    fun Bundle.putUserId(uid: String) = putString(EXTRA_USER_ID, uid)
    fun getUserId() = arguments?.getString(EXTRA_USER_ID)

    fun Bundle.putSudokuMatrixToExtra(matrix: IntMatrix?) = matrix
        .takeIf { it != null }
        ?.run { putParcelable(EXTRA_MATRIX, MatrixParcelModel(this)) }

    fun getSudokuMatrixFromExtra() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arguments?.getParcelable(EXTRA_MATRIX, MatrixParcelModel::class.java)
    } else {
        @Suppress("DEPRECATION")
        arguments?.getParcelable(EXTRA_MATRIX)
    }?.matrix?.run { CustomMatrix(this) }
}