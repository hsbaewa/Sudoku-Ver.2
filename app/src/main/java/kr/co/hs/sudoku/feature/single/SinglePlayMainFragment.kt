package kr.co.hs.sudoku.feature.single

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutSinglePlayMainBinding
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.feature.matrixlist.MatrixListItemAdapter
import kr.co.hs.sudoku.repository.AdvancedMatrixRepository
import kr.co.hs.sudoku.repository.BeginnerMatrixRepository
import kr.co.hs.sudoku.repository.IntermediateMatrixRepository


class SinglePlayMainFragment : Fragment(), AdapterView.OnItemSelectedListener {
    companion object {
        fun newInstance() = SinglePlayMainFragment()
    }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.spinnerDifficulty) {
            ArrayAdapter.createFromResource(
                context,
                R.array.difficulty,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears.
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner.
                this.adapter = adapter
            }
            onItemSelectedListener = this@SinglePlayMainFragment
        }

        with(binding.btnStart) {
            setOnClickListener { startSinglePlay() }
        }

        with(binding.recyclerViewMatrixList) {
            layoutManager = GridLayoutManager(context, 2)
            addVerticalDivider(thickness = 20.dp)
            itemAnimator?.removeDuration = 0
            itemAnimator?.addDuration = 500L
            itemAnimator?.changeDuration = 500L
            adapter = MatrixListItemAdapter()
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        when (context?.resources?.getStringArray(R.array.difficulty)?.get(p2)) {
            getString(R.string.beginner_matrix_size) -> setBeginnerMatrixView()
            getString(R.string.intermediate_matrix_size) -> setIntermediateMatrixView()
            getString(R.string.advanced_matrix_size) -> setAdvancedMatrixView()
            else -> {}
        }
    }

    private fun setBeginnerMatrixView() {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository = BeginnerMatrixRepository()
            val list = withContext(Dispatchers.IO) { repository.getList() }
            (binding.recyclerViewMatrixList.adapter as MatrixListItemAdapter).submitList(list)
        }
    }

    private fun setIntermediateMatrixView() {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository = IntermediateMatrixRepository()
            val list = withContext(Dispatchers.IO) { repository.getList() }
            (binding.recyclerViewMatrixList.adapter as MatrixListItemAdapter).submitList(list)
        }
    }

    private fun setAdvancedMatrixView() {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository = AdvancedMatrixRepository()
            val list = withContext(Dispatchers.IO) { repository.getList() }
            (binding.recyclerViewMatrixList.adapter as MatrixListItemAdapter).submitList(list.toList())
        }
    }


    override fun onNothingSelected(p0: AdapterView<*>?) {}

    private fun startSinglePlay() = viewLifecycleOwner.lifecycleScope.launch { }


}