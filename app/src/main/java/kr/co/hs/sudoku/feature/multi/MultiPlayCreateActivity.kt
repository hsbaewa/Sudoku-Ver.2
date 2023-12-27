package kr.co.hs.sudoku.feature.multi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.LayoutCreateMultiPlayBinding
import kr.co.hs.sudoku.extension.platform.ActivityExtension.isShowProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showSnackBar
import kr.co.hs.sudoku.feature.multi.play.MultiPlayWithAIActivity
import kr.co.hs.sudoku.feature.matrixlist.MatrixListViewModel
import kr.co.hs.sudoku.feature.matrixlist.MatrixSelectBottomSheetFragment
import kr.co.hs.sudoku.feature.multi.play.MultiPlayActivity
import kr.co.hs.sudoku.feature.multi.play.MultiPlayViewModel

class MultiPlayCreateActivity : Activity() {
    companion object {
        private fun newIntent(context: Context) =
            Intent(context, MultiPlayCreateActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

        fun start(context: Context) = context.startActivity(newIntent(context))
    }

    private val binding: LayoutCreateMultiPlayBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.layout_create_multi_play)
    }
    private val matrixListViewModel: MatrixListViewModel by viewModels()
    private val multiPlayViewModel: MultiPlayViewModel by viewModels {
        val app = applicationContext as App
        MultiPlayViewModel.ProviderFactory(app.getBattleRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        with(binding.cardView) {
            setOnClickListener { MatrixSelectBottomSheetFragment.showAll(supportFragmentManager) }
        }

        with(binding.matrix) {
            matrixListViewModel.selection.observe(this@MultiPlayCreateActivity) {
                matrix = it
                invalidate()
                MatrixSelectBottomSheetFragment.dismiss(supportFragmentManager)
            }
        }

        with(matrixListViewModel) {
            isRunningProgress.observe(this@MultiPlayCreateActivity) { isShowProgressIndicator = it }
            selectAny()
        }

        with(binding.btnCreate) {
            setOnClickListener {
                matrixListViewModel.selection.value
                    ?.run {
                        if (binding.checkboxWithAi.isChecked) {
                            startWithAI()
                        } else {
                            multiPlayViewModel.create(this)
                        }
                    }
                    ?: showSnackBar(getString(R.string.require_select_stage))
            }
        }

        with(multiPlayViewModel) {
            isRunningProgress.observe(this@MultiPlayCreateActivity) { isShowProgressIndicator = it }
            battleEntity.observe(this@MultiPlayCreateActivity) {
                if (it != null) {
                    startMultiPlay(it.id)
                }
            }
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigateUpToParent()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startMultiPlay(battleId: String?) = battleId?.let {
        lifecycleScope
            .launch { startActivity(MultiPlayActivity.newIntent(this@MultiPlayCreateActivity, it)) }
    }

    private fun startWithAI() = lifecycleScope.launch {
        matrixListViewModel.selection.value?.let { matrix ->
            startActivity(MultiPlayWithAIActivity.newIntent(this@MultiPlayCreateActivity, matrix))
        }

    }
}