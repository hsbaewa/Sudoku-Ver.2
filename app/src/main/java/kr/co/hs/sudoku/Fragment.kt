package kr.co.hs.sudoku

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.AdvancedMatrixRepository
import kr.co.hs.sudoku.repository.BeginnerMatrixRepository
import kr.co.hs.sudoku.repository.IntermediateMatrixRepository
import kr.co.hs.sudoku.repository.stage.MatrixRepository
import kr.co.hs.sudoku.viewmodel.StageListViewModel

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
    fun getStageListViewModel(repository: MatrixRepository<IntMatrix>): StageListViewModel {
        val viewModel: StageListViewModel
                by activityViewModels { StageListViewModel.Factory(repository) }
        return viewModel
    }

    fun getStageListViewModel(difficulty: Difficulty) = getStageListViewModel(
        when (difficulty) {
            Difficulty.BEGINNER -> BeginnerMatrixRepository()
            Difficulty.INTERMEDIATE -> IntermediateMatrixRepository()
            Difficulty.ADVANCED -> AdvancedMatrixRepository()
        }
    )

    fun getStageListViewModel(): StageListViewModel {
        val viewModel: StageListViewModel by activityViewModels()
        return viewModel
    }

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- conv -------------------------------------------\\
    //--------------------------------------------------------------------------------------------\\

    companion object {
        private const val EXTRA_LEVEL = "kr.co.hs.sudoku.Fragment.EXTRA_LEVEL"
    }

    val activity: Activity
        get() = super.getActivity() as Activity

    fun Bundle.putLevel(level: Int) = putInt(EXTRA_LEVEL, level)
    fun getLevel() = arguments?.getInt(EXTRA_LEVEL, 0) ?: 0
}