package kr.co.hs.sudoku.feature.battle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityCreateBattleBinding
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showSnackBar
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.BeginnerMatrixRepository
import kr.co.hs.sudoku.viewmodel.BattlePlayViewModel
import kr.co.hs.sudoku.viewmodel.SinglePlayDifficultyViewModel

class BattleCreateActivity : Activity() {
    companion object {
        private fun newIntent(context: Context) = Intent(context, BattleCreateActivity::class.java)
        fun startBattleCreateActivity(context: Context) = context.startActivity(newIntent(context))
    }

    private val binding: ActivityCreateBattleBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_create_battle)
    }

    // stage list viewmodel
    private val viewModel: SinglePlayDifficultyViewModel by viewModels()

    private val battlePlayViewModel: BattlePlayViewModel by viewModels {
        val app = applicationContext as App
        BattlePlayViewModel.ProviderFactory(app.getBattleRepository2())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this

        //--------------------------------------------------------------------------------------------\\
        //----------------------------------------- setup UI -----------------------------------------\\
        //--------------------------------------------------------------------------------------------\\

        with(binding) {
            sudokuBoardList.onCreatedStageList()
            btnCreate.onCreatedButton(onClickCreate(sudokuBoardList.adapter as SudokuMatrixListAdapter))
        }

        viewModel.matrixList.observe(this) { updateStageList(it) }

        lifecycleScope.launch {
            withStarted { viewModel.requestMatrix(BeginnerMatrixRepository()) }
        }

        battlePlayViewModel.battleEntity.observe(this) {
            if (it != null)
                finish()
        }
    }


    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- setup UI -----------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private fun RecyclerView.onCreatedStageList() {
        layoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
        adapter = SudokuMatrixListAdapter()
    }

    private fun Button.onCreatedButton(createBattle: () -> Unit) {
        setOnClickListener { createBattle() }
    }


    private fun updateStageList(list: List<IntMatrix>) {
        with(binding) {
            (sudokuBoardList.adapter as SudokuMatrixListAdapter).submitList(list)
        }
    }

    private fun onClickCreate(adapter: SudokuMatrixListAdapter): () -> Unit = {
        adapter.getSelectedItem()
            ?.let { battlePlayViewModel.create(it) }
            ?: showSnackBar(getString(R.string.require_select_stage))
    }
}