package kr.co.hs.sudoku.feature.single

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutSinglePlayMainBinding
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.platform.FragmentExtension.isShowProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showSnackBar
import kr.co.hs.sudoku.feature.matrixlist.MatrixListItem
import kr.co.hs.sudoku.feature.matrixlist.MatrixListItemAdapter
import kr.co.hs.sudoku.feature.matrixlist.MatrixListViewModel
import kr.co.hs.sudoku.feature.play.SinglePlayActivity.Companion.startPlayActivity
import kr.co.hs.sudoku.model.matrix.AdvancedMatrix
import kr.co.hs.sudoku.model.matrix.BeginnerMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.matrix.IntermediateMatrix


class SinglePlayMainFragment : Fragment() {
    companion object {
        private const val EXTRA_IS_TEST = "EXTRA_IS_TEST"
        private const val EXTRA_IS_DEBUG = "EXTRA_IS_DEBUG"
        @Suppress("unused")
        fun newTestArgument() = bundleOf(
            EXTRA_IS_TEST to true,
            EXTRA_IS_DEBUG to true
        )

        fun newInstance() = SinglePlayMainFragment()

        @Suppress("unused")
        fun newDebugInstance() = SinglePlayMainFragment().apply {
            arguments = bundleOf(
                EXTRA_IS_DEBUG to true
            )
        }
    }

    private val isTest: Boolean
        get() = arguments?.getBoolean(EXTRA_IS_TEST, false) ?: false
    @Suppress("unused")
    private val isDebug: Boolean
        get() = arguments?.getBoolean(EXTRA_IS_DEBUG, false) ?: false

    private lateinit var binding: LayoutSinglePlayMainBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutSinglePlayMainBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    private val viewModel: MatrixListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerViewMatrixList) {
            this.layoutManager = GridLayoutManager(context, 3)
            addVerticalDivider(thickness = 20.dp)
            adapter = MatrixListItemAdapter { it.startSinglePlay() }
        }

        viewModel.matrixList.observe(viewLifecycleOwner) { list -> updateUIMatrixList(list) }
        viewModel.error.observe(viewLifecycleOwner) { showSnackBar(it.message.toString()) }
        viewModel.isRunningProgress.observe(viewLifecycleOwner) { isShowProgressIndicator = it }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                isTest.takeIf { !it }?.run { viewModel.requestAllMatrix() }
            }
        }
    }

    fun updateUIMatrixList(list: List<IntMatrix>) {
        val headerForBeginner = getString(R.string.beginner_matrix_size)
        val headerForInter = getString(R.string.intermediate_matrix_size)
        val headerForAdvanced = getString(R.string.advanced_matrix_size)

        val adapterList = list.map { MatrixListItem.MatrixItem(it) }.toMutableList<MatrixListItem>()
            .apply {
                add(0, MatrixListItem.TitleItem(getString(R.string.caption_single_play)))

                indexOfFirst { it is MatrixListItem.MatrixItem && it.matrix is BeginnerMatrix }
                    .takeIf { it >= 0 }
                    ?.let { idx -> add(idx, MatrixListItem.HeaderItem(headerForBeginner)) }

                indexOfFirst { it is MatrixListItem.MatrixItem && it.matrix is IntermediateMatrix }
                    .takeIf { it >= 0 }
                    ?.let { idx -> add(idx, MatrixListItem.HeaderItem(headerForInter)) }

                indexOfFirst { it is MatrixListItem.MatrixItem && it.matrix is AdvancedMatrix }
                    .takeIf { it >= 0 }
                    ?.let { idx -> add(idx, MatrixListItem.HeaderItem(headerForAdvanced)) }
            }
        with(binding.recyclerViewMatrixList.layoutManager as GridLayoutManager) {
            spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int) =
                    when (adapterList[position]) {
                        is MatrixListItem.HeaderItem -> spanCount
                        is MatrixListItem.MatrixItem -> 1
                        is MatrixListItem.TitleItem -> spanCount
                    }
            }
        }

        with(binding.recyclerViewMatrixList.adapter as MatrixListItemAdapter) {
            submitList(adapterList)
        }
    }

    private fun IntMatrix.startSinglePlay() = viewLifecycleOwner.lifecycleScope.launch {
        activity.startPlayActivity(this@startSinglePlay)
    }
}