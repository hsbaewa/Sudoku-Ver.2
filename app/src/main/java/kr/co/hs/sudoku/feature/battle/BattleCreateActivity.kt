package kr.co.hs.sudoku.feature.battle

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityCreateBattleBinding
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showSnackBar
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.BeginnerMatrixRepository
import kr.co.hs.sudoku.viewmodel.SinglePlayDifficultyViewModel

class BattleCreateActivity : Activity() {
    companion object {
        private fun Activity.newIntent(uid: String) = Intent(this, BattleCreateActivity::class.java)
            .putUserId(uid)

        fun Activity.startBattleCreateActivity(uid: String) =
            startActivity(newIntent(uid))
    }

    lateinit var binding: ActivityCreateBattleBinding
    private val matrixListAdapter: SudokuMatrixListAdapter by lazy { SudokuMatrixListAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_battle)
        binding.lifecycleOwner = this

        //--------------------------------------------------------------------------------------------\\
        //----------------------------------------- setup UI -----------------------------------------\\
        //--------------------------------------------------------------------------------------------\\

        binding.sudokuBoardList.setupUIStageList(stageListViewModel.matrixList)
        binding.btnCreate.setupUICreateButton()

        lifecycleScope.launch {
            withStarted {
                singlePlayDifficultyViewModels().run {
                    requestMatrix(BeginnerMatrixRepository())
                }
            }
        }
    }

    // stage list viewmodel
    private val stageListViewModel: SinglePlayDifficultyViewModel by lazy { singlePlayDifficultyViewModels() }

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- setup UI -----------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private fun RecyclerView.setupUIStageList(data: LiveData<List<IntMatrix>>) {
        layoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
        adapter = matrixListAdapter

        data.observe(this@BattleCreateActivity) {
            matrixListAdapter.submitList(it)
        }
    }

    private fun Button.setupUICreateButton() {
        setOnClickListener {
            val matrix = matrixListAdapter.getSelectedItem() ?: return@setOnClickListener

            lifecycleScope.launch {
                getUserId()
                    ?.let { uid ->
                        val profile = withContext(Dispatchers.IO) { getProfile(uid) }
                        withContext(Dispatchers.IO) {
                            app.getBattleRepository().createBattle(profile, matrix)
                        }
                        finish()
                    }
                    ?: kotlin.run {
                        showSnackBar(getString(R.string.error_require_authenticate))
                    }

            }
        }

    }

}