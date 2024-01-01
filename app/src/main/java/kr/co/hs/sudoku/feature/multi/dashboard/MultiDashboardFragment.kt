package kr.co.hs.sudoku.feature.multi.dashboard

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
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showSnackBar
import kr.co.hs.sudoku.feature.multi.MultiPlayCreateActivity
import kr.co.hs.sudoku.feature.multi.play.MultiPlayActivity
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.feature.multi.play.MultiPlayViewModel
import kr.co.hs.sudoku.views.RecyclerView

class MultiDashboardFragment : Fragment() {
    companion object {
        fun newInstance() = MultiDashboardFragment()
    }

    private lateinit var binding: LayoutListMultiPlayBinding
    private val dashboardViewModel: MultiDashboardViewModel by activityViewModels()
    private val playViewModel: MultiPlayViewModel by activityViewModels()

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

        dashboardViewModel.currentMultiPlay.observe(viewLifecycleOwner) { startMultiPlay(it?.id) }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { dashboardViewModel.checkCurrentMultiPlay() }
        }

        with(binding.swipeRefreshLayout) {
            setOnRefreshListener { refreshMultiPlayListData() }
        }
    }

    private fun RecyclerView.onCreatedRecyclerViewMultiPlay() {
        layoutManager = LinearLayoutManager(context)
        addVerticalDivider(thickness = 10.dp)
        val pagingDataAdapter = MultiDashboardListItemAdapter(
            onItemClick = { showConfirmJoinMultiPlay(it) },
            onCreateNew = { startCreateMulti() }
        )

        pagingDataAdapter.apply {
            addLoadStateListener { loadState ->
                when (loadState.refresh) {
                    is LoadState.NotLoading, is LoadState.Error -> {
                        dismissRefreshing()
                        dismissProgressIndicator()
                    }

                    LoadState.Loading -> {
                        if (!isRefreshing()) {
                            showProgressIndicator()
                        }
                    }

                }

                if (loadState.refresh is LoadState.NotLoading && loadState.append is LoadState.NotLoading) {
                    // 페이지 로드 완료 후 동작
                    dismissRefreshing()

                    with(binding.tvEmptyMessage) {
                        isVisible = snapshot()
                            .filterIsInstance<MultiDashboardListItem.MultiPlayItem>()
                            .isEmpty()
                    }
                }
            }
        }

        adapter = pagingDataAdapter.withLoadStateFooter(PagingLoadStateAdapter())
    }

    private fun submitMultiPlayListData() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            dashboardViewModel.multiPlayPagingData.observe(viewLifecycleOwner) {
                getMultiPlayListItemAdapter().submitData(lifecycle, it)
            }
        }
    }

    private fun getMultiPlayListItemAdapter() =
        (binding.recyclerViewMultiPlayList.adapter as ConcatAdapter).adapters.find { it is MultiDashboardListItemAdapter } as MultiDashboardListItemAdapter

    private fun refreshMultiPlayListData() {
        getMultiPlayListItemAdapter().refresh()
    }

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
            withContext(Dispatchers.IO) { playViewModel.doJoin(battleEntity.id) }
            dismissProgressIndicator()
            startMultiPlay(battleEntity.id)
        }

    private fun startMultiPlay(battleId: String?) = battleId?.let {
        viewLifecycleOwner.lifecycleScope
            .launch { startActivity(MultiPlayActivity.newIntent(requireContext(), it)) }
    }

    private fun isRefreshing() = binding.swipeRefreshLayout.isRefreshing
    private fun dismissRefreshing() {
        binding.swipeRefreshLayout.isRefreshing = false
    }
}