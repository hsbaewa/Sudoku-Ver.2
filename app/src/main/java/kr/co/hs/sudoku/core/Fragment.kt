package kr.co.hs.sudoku.core

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.AdvancedMatrixRepository
import kr.co.hs.sudoku.repository.BeginnerMatrixRepository
import kr.co.hs.sudoku.repository.IntermediateMatrixRepository
import kr.co.hs.sudoku.repository.settings.GameSettingsRepository
import kr.co.hs.sudoku.repository.stage.MatrixRepository
import kr.co.hs.sudoku.viewmodel.GameSettingsViewModel
import kr.co.hs.sudoku.viewmodel.SudokuStatusViewModel
import kr.co.hs.sudoku.viewmodel.SudokuStageViewModel
import kr.co.hs.sudoku.viewmodel.TimerLogViewModel

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
    private fun sudokuStageViewModels(repository: MatrixRepository<IntMatrix>): SudokuStageViewModel {
        val viewModel: SudokuStageViewModel
                by activityViewModels { SudokuStageViewModel.Factory(repository) }
        return viewModel
    }

    protected fun sudokuStageViewModels(difficulty: Activity.Difficulty) = sudokuStageViewModels(
        when (difficulty) {
            Activity.Difficulty.BEGINNER -> BeginnerMatrixRepository()
            Activity.Difficulty.INTERMEDIATE -> IntermediateMatrixRepository()
            Activity.Difficulty.ADVANCED -> AdvancedMatrixRepository()
        }
    )

    protected fun sudokuStageViewModels(): SudokuStageViewModel {
        val viewModel: SudokuStageViewModel by activityViewModels()
        return viewModel
    }

    protected fun gameSettingsViewModels(repository: GameSettingsRepository): GameSettingsViewModel {
        val viewModel: GameSettingsViewModel
                by activityViewModels { GameSettingsViewModel.Factory(repository) }
        return viewModel
    }

    protected fun sudokuStatusViewModels(): SudokuStatusViewModel {
        val viewModel: SudokuStatusViewModel by activityViewModels()
        return viewModel
    }

    protected fun timerLogViewModels(): TimerLogViewModel {
        val viewModel: TimerLogViewModel by activityViewModels()
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