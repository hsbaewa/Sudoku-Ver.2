package kr.co.hs.sudoku.feature.matrixlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutListMatrixBinding
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.model.matrix.IntMatrix

class MatrixSelectBottomSheetFragment : BottomSheetDialogFragment() {
    companion object {
        private const val EXTRA_LEVEL = "EXTRA_LEVEL"
        private const val LEVEL_BEGINNER = "LEVEL_BEGINNER"
        private const val LEVEL_INTERMEDIATE = "LEVEL_INTERMEDIATE"
        private const val LEVEL_ADVANCED = "LEVEL_ADVANCED"
        private const val LEVEL_ALL = "LEVEL_ALL"

        fun showBeginner(fragmentManager: FragmentManager) = MatrixSelectBottomSheetFragment()
            .apply { arguments = bundleOf(EXTRA_LEVEL to LEVEL_BEGINNER) }
            .show(fragmentManager, MatrixSelectBottomSheetFragment::class.java.name)

        fun showIntermediate(fragmentManager: FragmentManager) = MatrixSelectBottomSheetFragment()
            .apply { arguments = bundleOf(EXTRA_LEVEL to LEVEL_INTERMEDIATE) }
            .show(fragmentManager, MatrixSelectBottomSheetFragment::class.java.name)

        fun showAdvanced(fragmentManager: FragmentManager) = MatrixSelectBottomSheetFragment()
            .apply { arguments = bundleOf(EXTRA_LEVEL to LEVEL_ADVANCED) }
            .show(fragmentManager, MatrixSelectBottomSheetFragment::class.java.name)

        fun showAll(fragmentManager: FragmentManager) = MatrixSelectBottomSheetFragment()
            .apply { arguments = bundleOf(EXTRA_LEVEL to LEVEL_ALL) }
            .show(fragmentManager, MatrixSelectBottomSheetFragment::class.java.name)

        fun dismiss(fragmentManager: FragmentManager) {
            val fragment =
                fragmentManager.findFragmentByTag(MatrixSelectBottomSheetFragment::class.java.name) as? MatrixSelectBottomSheetFragment
            fragment?.run { dismiss() }
        }
    }

    private lateinit var binding: LayoutListMatrixBinding
    private val viewModel: MatrixListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutListMatrixBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerViewMatrixList) {
            this.layoutManager = MatrixListLayoutManager(context, 3)
            addVerticalDivider(thickness = 20.dp)
            val adapter = MatrixListItemAdapter { viewModel.select(it) }
            this.adapter = adapter

            viewModel.matrixList.observe(viewLifecycleOwner) { submitList(it) }
        }

        viewLifecycleOwner.lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.STARTED) { requestMatrix() } }
    }


    private fun submitList(list: List<IntMatrix>) =
        with(binding.recyclerViewMatrixList.adapter as MatrixListItemAdapter) {
            submitHeaderList(
                getString(R.string.select_matrix_title),
                list.map { item -> MatrixListItem.MatrixItem(item) }.toMutableList()
            )
        }


    private enum class Level { Beginner, Intermediate, Advanced, All }

    private fun getLevel() = when (arguments?.getString(EXTRA_LEVEL)) {
        LEVEL_BEGINNER -> Level.Beginner
        LEVEL_INTERMEDIATE -> Level.Intermediate
        LEVEL_ADVANCED -> Level.Advanced
        else -> Level.All
    }

    private fun requestMatrix() = with(viewModel) {
        when (getLevel()) {
            Level.Beginner -> requestBeginnerMatrix()
            Level.Intermediate -> requestIntermediateMatrix()
            Level.Advanced -> requestAdvancedMatrix()
            Level.All -> requestAllMatrix()
        }
    }

}