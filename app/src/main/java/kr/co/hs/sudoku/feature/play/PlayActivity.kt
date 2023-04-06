package kr.co.hs.sudoku.feature.play

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.ActivityPlayBinding
import kr.co.hs.sudoku.extension.platform.ActivityExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.replaceFragment
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showProgressIndicator
import kr.co.hs.sudoku.viewmodel.SudokuViewModel

class PlayActivity : Activity() {
    companion object {
        fun Activity.startPlayActivity(difficulty: Difficulty, level: Int) =
            startActivity(
                Intent(this, PlayActivity::class.java)
                    .putDifficulty(difficulty)
                    .putLevel(level)
            )
    }

    private val sudokuViewModel: SudokuViewModel
            by lazy { sudokuViewModels(getDifficulty()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityPlayBinding>(this, R.layout.activity_play)

        sudokuViewModel.matrixList.observe(this) {
            dismissProgressIndicator()
            replaceFragment(R.id.rootLayout, PlayFragment.new(getLevel()))
        }

        lifecycleScope.launch {
            withStarted {
                showProgressIndicator()
                sudokuViewModel.requestStageList()
            }
        }
    }
}