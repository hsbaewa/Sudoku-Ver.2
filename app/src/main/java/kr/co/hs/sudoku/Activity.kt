package kr.co.hs.sudoku

import android.content.Intent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.AdvancedMatrixRepository
import kr.co.hs.sudoku.repository.BeginnerMatrixRepository
import kr.co.hs.sudoku.repository.IntermediateMatrixRepository
import kr.co.hs.sudoku.repository.stage.MatrixRepository
import kr.co.hs.sudoku.viewmodel.StageListViewModel

abstract class Activity : AppCompatActivity() {
    //--------------------------------------------------------------------------------------------\\
    //-----------------------------------------  ViewModel Provider 관련  ----------------------------------\\
    //--------------------------------------------------------------------------------------------\\

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment ViewModel Getter
     * @return StageListViewModel
     **/
    fun getStageListViewModel(repository: MatrixRepository<IntMatrix>): StageListViewModel {
        val viewModel: StageListViewModel by viewModels { StageListViewModel.Factory(repository) }
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
        val viewModel: StageListViewModel by viewModels()
        return viewModel
    }


    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- conv -------------------------------------------\\
    //--------------------------------------------------------------------------------------------\\

    companion object {
        private const val EXTRA_DIFFICULTY = "kr.co.hs.sudoku.EXTRA_DIFFICULTY"
        private const val EXTRA_LEVEL = "kr.co.hs.sudoku.EXTRA_LEVEL"
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/05
     * @comment Intent로 전달 받은 Difficulty
     * @return Difficulty
     **/
    fun getDifficulty() = intent.getStringExtra(EXTRA_DIFFICULTY)
        ?.runCatching { Difficulty.valueOf(this) }
        ?.getOrDefault(Difficulty.BEGINNER)
        ?: Difficulty.BEGINNER

    fun Intent.putDifficulty(difficulty: Difficulty) = putExtra(EXTRA_DIFFICULTY, difficulty.name)

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment 전달받은 Level을 intent로부터 반환
     * @return 전달받은 Level Int 값
     **/
    fun getLevel() = intent.getIntExtra(EXTRA_LEVEL, 0)

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment intent를 통해 level 전달
     * @param level 전달 할 level
     * @return Intent
     **/
    fun Intent.putLevel(level: Int) = putExtra(EXTRA_LEVEL, level)
}