package kr.co.hs.sudoku.feature.play

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.Activity
import kr.co.hs.sudoku.Difficulty
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.ActivityPlayBinding
import kr.co.hs.sudoku.extension.ActivityExtension.replaceFragment
import kr.co.hs.sudoku.viewmodel.StageListViewModel

class PlayActivity : Activity() {
    companion object {
        fun Activity.startPlayActivity(difficulty: Difficulty, level: Int) =
            startActivity(
                Intent(this, PlayActivity::class.java)
                    .putDifficulty(difficulty)
                    .putLevel(level)
            )
    }

    private val stageListViewModel: StageListViewModel
            by lazy { getStageListViewModel(getDifficulty()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityPlayBinding>(this, R.layout.activity_play)

        lifecycleScope.launch {
            stageListViewModel.doRequestStageList()
            withStarted { replaceFragment(R.id.rootLayout, PlayFragment.new(getLevel())) }
        }

    }
}