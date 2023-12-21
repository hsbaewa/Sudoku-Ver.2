package kr.co.hs.sudoku.feature.multi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.paging.LoadState
import androidx.paging.TerminalSeparatorType
import androidx.paging.insertHeaderItem
import androidx.paging.map
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.core.PagingLoadStateAdapter
import kr.co.hs.sudoku.databinding.LayoutListMultiPlayBinding
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showProgressIndicator
import kr.co.hs.sudoku.model.battle.BattleEntity

class MultiPlayListFragment : Fragment() {
    companion object {
        fun newInstance() = MultiPlayListFragment()
    }

    private lateinit var binding: LayoutListMultiPlayBinding
    private val viewModel: MultiPlayListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutListMultiPlayBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerViewMultiPlayList) {
            layoutManager = LinearLayoutManager(context)
            addVerticalDivider(thickness = 10.dp)
            val pagingDataAdapter = MultiPlayListItemAdapter(
                onItemClick = {},
                onCreateNew = {}
            )

            pagingDataAdapter.apply {
                addLoadStateListener { loadState ->
                    when (loadState.refresh) {
                        is LoadState.NotLoading -> dismissProgressIndicator()
                        LoadState.Loading -> showProgressIndicator()
                        is LoadState.Error -> dismissProgressIndicator()
                    }

                    @Suppress("ControlFlowWithEmptyBody")
                    if (loadState.refresh is LoadState.NotLoading && loadState.append is LoadState.NotLoading) {
                        // 페이지 로드 완료 후 동작
                    }
                }
            }

            adapter = pagingDataAdapter.withLoadStateFooter(PagingLoadStateAdapter())
        }

        submitData()
    }

    private fun submitData() = viewLifecycleOwner.lifecycleScope.launch {
        withStarted {
            val pagingDataAdapter: MultiPlayListItemAdapter =
                (binding.recyclerViewMultiPlayList.adapter as ConcatAdapter).adapters.find { it is MultiPlayListItemAdapter } as MultiPlayListItemAdapter

            viewModel.multiPlayPagingData.observe(viewLifecycleOwner) {
                pagingDataAdapter.submitData(
                    lifecycle,
                    it.insertHeaderItem(TerminalSeparatorType.FULLY_COMPLETE, NewCreateEntity)
                        .insertHeaderItem(TerminalSeparatorType.FULLY_COMPLETE, TitleEntity)
                        .map { entity ->
                            when (entity) {
                                TitleEntity -> MultiPlayListItem.TitleItem(getString(R.string.title_multi_play))
                                NewCreateEntity -> MultiPlayListItem.CreateNewItem
                                else -> MultiPlayListItem.MultiPlayItem(entity)
                            }
                        }
                )
            }
        }
    }

    object TitleEntity : BattleEntity.Invalid()

    object NewCreateEntity : BattleEntity.Invalid()
}