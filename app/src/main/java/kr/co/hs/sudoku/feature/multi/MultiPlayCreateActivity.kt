package kr.co.hs.sudoku.feature.multi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.LayoutCreateMultiPlayBinding
import kr.co.hs.sudoku.extension.platform.ActivityExtension.isShowProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showSnackBar
import kr.co.hs.sudoku.feature.matrixlist.MatrixListViewModel
import kr.co.hs.sudoku.feature.matrixlist.MatrixSelectBottomSheetFragment
import kr.co.hs.sudoku.viewmodel.BattlePlayViewModel

class MultiPlayCreateActivity : Activity() {
    companion object {
        private fun newIntent(context: Context) =
            Intent(context, MultiPlayCreateActivity::class.java)

        fun start(context: Context) = context.startActivity(newIntent(context))
    }

    private val binding: LayoutCreateMultiPlayBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.layout_create_multi_play)
    }
    private val matrixListViewModel: MatrixListViewModel by viewModels()
    private val battlePlayViewModel: BattlePlayViewModel by viewModels {
        val app = applicationContext as App
        BattlePlayViewModel.ProviderFactory(app.getBattleRepository2())
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
                    ?.run { battlePlayViewModel.create(this) }
                    ?: showSnackBar(getString(R.string.require_select_stage))
            }
        }

        with(battlePlayViewModel) {
            isRunningProgress.observe(this@MultiPlayCreateActivity) { isShowProgressIndicator = it }
            battleEntity.observe(this@MultiPlayCreateActivity) {
                if (it != null) {
                    navigateUpToParent()
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
}