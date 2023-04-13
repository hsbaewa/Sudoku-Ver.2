package kr.co.hs.sudoku.core

import android.content.Intent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.games.PlayGames
import com.google.firebase.auth.FirebaseAuth
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.AdvancedMatrixRepository
import kr.co.hs.sudoku.repository.BeginnerMatrixRepository
import kr.co.hs.sudoku.repository.IntermediateMatrixRepository
import kr.co.hs.sudoku.repository.stage.MatrixRepository
import kr.co.hs.sudoku.viewmodel.TimerLogViewModel
import kr.co.hs.sudoku.viewmodel.SudokuStageViewModel

abstract class Activity : AppCompatActivity() {
    enum class Difficulty { BEGINNER, INTERMEDIATE, ADVANCED }

    //--------------------------------------------------------------------------------------------\\
    //-----------------------------------------  ViewModel Provider 관련  ----------------------------------\\
    //--------------------------------------------------------------------------------------------\\

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment ViewModel Getter
     * @return StageListViewModel
     **/
    private fun sudokuStageViewModels(repository: MatrixRepository<IntMatrix>): SudokuStageViewModel {
        val viewModel: SudokuStageViewModel by viewModels { SudokuStageViewModel.Factory(repository) }
        return viewModel
    }

    protected fun sudokuStageViewModels(difficulty: Difficulty) = sudokuStageViewModels(
        when (difficulty) {
            Difficulty.BEGINNER -> BeginnerMatrixRepository()
            Difficulty.INTERMEDIATE -> IntermediateMatrixRepository()
            Difficulty.ADVANCED -> AdvancedMatrixRepository()
        }
    )

    private fun sudokuStageViewModels(): SudokuStageViewModel {
        val viewModel: SudokuStageViewModel by viewModels()
        return viewModel
    }

    protected fun timerLogViewModels(): TimerLogViewModel {
        val viewModel: TimerLogViewModel by viewModels()
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
    protected fun getDifficulty() = intent.getStringExtra(EXTRA_DIFFICULTY)
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
    protected fun getLevel() = intent.getIntExtra(EXTRA_LEVEL, 0)

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment intent를 통해 level 전달
     * @param level 전달 할 level
     * @return Intent
     **/
    fun Intent.putLevel(level: Int) = putExtra(EXTRA_LEVEL, level)

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment Games 계정과 Firebase 계정 마이그레이션, Games 계정이 로그인 되어 있는 경우 해당 Credential로 Firebase에 로그인 시킨다
     **/
    protected suspend fun syncAuthenticate() = createAuthenticateMediator().sync()

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment AuthenticateMediator 생성
     * @return AuthenticateMediator
     **/
    private fun createAuthenticateMediator() = AuthenticateMediator(
        PlayGames.getGamesSignInClient(this),
        FirebaseAuth.getInstance(),
        getString(R.string.default_web_client_id)
    )
}