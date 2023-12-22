package kr.co.hs.sudoku.feature.multi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.TerminalSeparatorType
import androidx.paging.insertHeaderItem
import androidx.paging.map
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.core.PagingLoadStateAdapter
import kr.co.hs.sudoku.databinding.LayoutListMultiPlayBinding
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.platform.ContextExtension.getColorCompat
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showSnackBar
import kr.co.hs.sudoku.feature.multiplay.MultiGameActivity
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.viewmodel.BattlePlayViewModel
import kr.co.hs.sudoku.views.RecyclerView

class MultiPlayListFragment : Fragment() {
    companion object {
        fun newInstance() = MultiPlayListFragment()
    }

    private lateinit var binding: LayoutListMultiPlayBinding
    private val viewModel: MultiPlayListViewModel by activityViewModels()
    private val battleViewModel: BattlePlayViewModel by activityViewModels()

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

        binding.recyclerViewMultiPlayList.onCreatedRecyclerViewMultiPlay()
        submitMultiPlayListData()

        viewModel.currentMultiPlay.observe(viewLifecycleOwner) { startMultiPlay(it) }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.checkCurrentMultiPlay()
            }
        }

        with(binding.swipeRefreshLayout) {
            setColorSchemeColors(context.getColorCompat(R.color.gray_500))
            setOnRefreshListener { refreshMultiPlayListData() }
        }
    }

    private fun RecyclerView.onCreatedRecyclerViewMultiPlay() {
        layoutManager = LinearLayoutManager(context)
        addVerticalDivider(thickness = 10.dp)
        val pagingDataAdapter = MultiPlayListItemAdapter(
            onItemClick = { showConfirmJoinMultiPlay(it) },
            onCreateNew = { startCreateMulti() }
        )

        pagingDataAdapter.apply {
            addLoadStateListener { loadState ->
                when (loadState.refresh) {
                    is LoadState.NotLoading -> dismissProgressIndicator()
                    LoadState.Loading -> showProgressIndicator()
                    is LoadState.Error -> dismissProgressIndicator()
                }

                if (loadState.refresh is LoadState.NotLoading && loadState.append is LoadState.NotLoading) {
                    // 페이지 로드 완료 후 동작
                    with(binding.swipeRefreshLayout) {
                        isRefreshing = false
                    }


                    with(binding.tvEmptyMessage) {
                        isVisible = snapshot()
                            .filterIsInstance<MultiPlayListItem.MultiPlayItem>()
                            .isEmpty()
                    }
                }
            }
        }

        adapter = pagingDataAdapter.withLoadStateFooter(PagingLoadStateAdapter())
    }

    private fun submitMultiPlayListData() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.multiPlayPagingData.observe(viewLifecycleOwner) {
                getMultiPlayListItemAdapter().submitData(
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

    private fun getMultiPlayListItemAdapter() =
        (binding.recyclerViewMultiPlayList.adapter as ConcatAdapter).adapters.find { it is MultiPlayListItemAdapter } as MultiPlayListItemAdapter

    private fun refreshMultiPlayListData() {
        getMultiPlayListItemAdapter().refresh()
    }

    object TitleEntity : BattleEntity.Invalid()

    object NewCreateEntity : BattleEntity.Invalid()

    private fun startCreateMulti() = viewLifecycleOwner.lifecycleScope.launch {
        MultiPlayCreateActivity.start(requireContext())
    }

    private fun showConfirmJoinMultiPlay(battleEntity: BattleEntity) =
        viewLifecycleOwner.lifecycleScope.launch {
            showConfirm(
                msgResId = R.string.multi_list_join_confirm,
                onConfirm = {
                    if (it) {
                        joinMultiPlay(battleEntity)
                    }
                }
            )
        }

    private fun joinMultiPlay(battleEntity: BattleEntity) =
        viewLifecycleOwner.lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            dismissProgressIndicator()
            showSnackBar(throwable.message.toString())
        }) {
            showProgressIndicator()
            withContext(Dispatchers.IO) { battleViewModel.doJoin(battleEntity.id) }
            dismissProgressIndicator()
            startMultiPlay(battleEntity)
        }

    private fun startMultiPlay(battleEntity: BattleEntity?) = battleEntity
        ?.run {
            viewLifecycleOwner.lifecycleScope
                .launch { startActivity(MultiGameActivity.newIntent(requireContext(), id)) }
        }
}