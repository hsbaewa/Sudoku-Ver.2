package kr.co.hs.sudoku.core

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.AdvancedMatrixRepository
import kr.co.hs.sudoku.repository.BeginnerMatrixRepository
import kr.co.hs.sudoku.repository.IntermediateMatrixRepository
import kr.co.hs.sudoku.repository.stage.MatrixRepository
import kr.co.hs.sudoku.viewmodel.SudokuViewModel

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
    private fun sudokuViewModels(repository: MatrixRepository<IntMatrix>): SudokuViewModel {
        val viewModel: SudokuViewModel
                by activityViewModels { SudokuViewModel.Factory(repository) }
        return viewModel
    }

    protected fun sudokuViewModels(difficulty: Activity.Difficulty) = sudokuViewModels(
        when (difficulty) {
            Activity.Difficulty.BEGINNER -> BeginnerMatrixRepository()
            Activity.Difficulty.INTERMEDIATE -> IntermediateMatrixRepository()
            Activity.Difficulty.ADVANCED -> AdvancedMatrixRepository()
        }
    )

    protected fun sudokuViewModels(): SudokuViewModel {
        val viewModel: SudokuViewModel by activityViewModels()
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