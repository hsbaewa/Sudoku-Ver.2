package kr.co.hs.sudoku.core

import android.content.Intent
import android.os.Build
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.flow.last
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.parcel.MatrixParcelModel
import kr.co.hs.sudoku.usecase.user.GetProfileUseCase
import kr.co.hs.sudoku.viewmodel.ChallengeViewModel
import kr.co.hs.sudoku.viewmodel.GamePlayViewModel
import kr.co.hs.sudoku.viewmodel.RecordViewModel
import kr.co.hs.sudoku.viewmodel.SinglePlayDifficultyViewModel

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
    protected fun gamePlayViewModels(): GamePlayViewModel {
        val viewModel: GamePlayViewModel by viewModels()
        return viewModel
    }

    protected fun singlePlayDifficultyViewModels(): SinglePlayDifficultyViewModel {
        val viewModel: SinglePlayDifficultyViewModel by viewModels()
        return viewModel
    }

    protected fun challengeRankingViewModels(): ChallengeViewModel {
        val viewModel: ChallengeViewModel by viewModels()
        return viewModel
    }

    protected fun recordViewModels(): RecordViewModel {
        val viewModel: RecordViewModel by viewModels()
        return viewModel
    }

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- conv -------------------------------------------\\
    //--------------------------------------------------------------------------------------------\\

    protected val app: App by lazy { applicationContext as App }

    protected suspend fun getProfile(uid: String) =
        GetProfileUseCase(app.getProfileRepository()).invoke(uid).last()


    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- Extra Data ----------------------------------------\\
    //--------------------------------------------------------------------------------------------\\

    companion object {
        private const val EXTRA_MATRIX = "kr.co.hs.sudoku.EXTRA_MATRIX"
        private const val EXTRA_DIFFICULTY = "kr.co.hs.sudoku.EXTRA_DIFFICULTY"
        private const val EXTRA_CHALLENGE_ID = "kr.co.hs.sudoku.EXTRA_CHALLENGE_ID"
        private const val EXTRA_USER_ID = "kr.co.hs.sudoku.EXTRA_USER_ID"
        private const val EXTRA_BATTLE_ID = "kr.co.hs.sudoku.EXTRA_BATTLE_ID"
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


    fun Intent.putChallengeId(challengeId: String) = putExtra(EXTRA_CHALLENGE_ID, challengeId)

    fun getExtraForChallengeId() = intent.getStringExtra(EXTRA_CHALLENGE_ID)

    fun Intent.putSudokuMatrix(matrix: IntMatrix?) = matrix
        .takeIf { it != null }
        ?.run { putExtra(EXTRA_MATRIX, MatrixParcelModel(this)) }

    fun getSudokuMatrix() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getParcelableExtra(EXTRA_MATRIX, MatrixParcelModel::class.java)
    } else {
        @Suppress("DEPRECATION")
        intent.getParcelableExtra(EXTRA_MATRIX)
    }?.matrix?.run { CustomMatrix(this) }

    fun Intent.putUserId(uid: String) = putExtra(EXTRA_USER_ID, uid)
    fun getUserId() = intent.getStringExtra(EXTRA_USER_ID)

    fun Intent.putBattleId(battleId: String) = putExtra(EXTRA_BATTLE_ID, battleId)
    fun getBattleId() = intent.getStringExtra(EXTRA_BATTLE_ID)
}