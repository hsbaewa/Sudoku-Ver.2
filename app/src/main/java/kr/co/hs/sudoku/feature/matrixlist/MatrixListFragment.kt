package kr.co.hs.sudoku.feature.matrixlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutMatrixListBinding
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showSnackBar
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.stage.MatrixRepository
import kr.co.hs.sudoku.views.RecyclerView

abstract class MatrixListFragment : Fragment() {
    abstract val repository: MatrixRepository<IntMatrix>
    private val viewModel: MatrixListViewModel by viewModels {
        MatrixListViewModel.ProviderFactory(repository)
    }

    private lateinit var binding: LayoutMatrixListBinding
    private val matrixList: RecyclerView by lazy {
        binding.recyclerViewMatrixList.apply {
            layoutManager = GridLayoutManager(context, 2)
            addVerticalDivider(thickness = 20.dp)
        }
    }
    private val matrixListAdapter: MatrixListItemAdapter by lazy { MatrixListItemAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutMatrixListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        matrixList.adapter = matrixListAdapter

        with(viewModel) {
            error.observe(viewLifecycleOwner) { showSnackBar(it.message.toString()) }
            matrixList.observe(viewLifecycleOwner) { setList(it) }
            viewLifecycleOwner.lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.STARTED) { viewModel.requestList() } }
        }
    }

    fun setList(list: List<IntMatrix>) = matrixListAdapter.submitList(list)
}