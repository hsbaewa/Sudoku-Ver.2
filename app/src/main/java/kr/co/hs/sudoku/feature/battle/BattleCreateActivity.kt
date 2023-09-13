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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityCreateBattleBinding
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showSnackBar
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.BeginnerMatrixRepository
import kr.co.hs.sudoku.repository.battle.BattleRepository
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
            ?.let { selectedMatrix ->
                app.getBattleRepository().submitCreateBattle(selectedMatrix)
            }
            ?: showSnackBar(getString(R.string.require_select_stage))
    }

    private fun BattleRepository.submitCreateBattle(matrix: IntMatrix) =
        lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            showSnackBar(throwable.message.toString())
        }) {
            withContext(Dispatchers.IO) {
                val uid = currentUser?.uid
                    ?: throw Exception(getString(R.string.error_require_authenticate))
                val profile = getProfile(uid)
                createBattle(profile, matrix)
            }
            finish()
        }

    private val currentUser: FirebaseUser?
        get() = FirebaseAuth.getInstance().currentUser
}