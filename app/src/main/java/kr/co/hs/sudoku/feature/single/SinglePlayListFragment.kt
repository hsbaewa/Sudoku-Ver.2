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
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutListSinglePlayBinding
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.platform.FragmentExtension.isShowProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showSnackBar
import kr.co.hs.sudoku.feature.matrixlist.MatrixListItem
import kr.co.hs.sudoku.feature.matrixlist.MatrixListItemAdapter
import kr.co.hs.sudoku.feature.matrixlist.MatrixListLayoutManager
import kr.co.hs.sudoku.feature.matrixlist.MatrixListViewModel
import kr.co.hs.sudoku.model.matrix.IntMatrix


class SinglePlayListFragment : Fragment() {
    companion object {
        private const val EXTRA_IS_TEST = "EXTRA_IS_TEST"
        private const val EXTRA_IS_DEBUG = "EXTRA_IS_DEBUG"

        @Suppress("unused")
        fun newTestArgument() = bundleOf(
            EXTRA_IS_TEST to true,
            EXTRA_IS_DEBUG to true
        )

        fun newInstance() = SinglePlayListFragment()

        @Suppress("unused")
        fun newDebugInstance() = SinglePlayListFragment().apply {
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

    private lateinit var binding: LayoutListSinglePlayBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutListSinglePlayBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    private val viewModel: MatrixListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerViewMatrixList) {
            this.layoutManager = MatrixListLayoutManager(context, 3)
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
        with(binding.recyclerViewMatrixList.adapter as MatrixListItemAdapter) {
            submitHeaderList(
                getString(R.string.title_single_play),
                list.map { MatrixListItem.MatrixItem(it) }.toMutableList()
            )
        }
    }

    private fun IntMatrix.startSinglePlay() = viewLifecycleOwner.lifecycleScope.launch {
        SinglePlayActivity.start(requireContext(), this@startSinglePlay)
    }
}