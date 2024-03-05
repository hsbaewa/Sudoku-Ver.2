package kr.co.hs.sudoku.feature.challenge.dashboard

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.core.PagingLoadStateAdapter
import kr.co.hs.sudoku.databinding.LayoutListChallengeBinding
import kr.co.hs.sudoku.di.repositories.ChallengeRepositoryQualifier
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showProgressIndicator
import kr.co.hs.sudoku.feature.ad.ChallengeRetryRewardAdManager
import kr.co.hs.sudoku.feature.challenge.play.ChallengePlayActivity
import kr.co.hs.sudoku.feature.leaderboard.LeaderBoardBottomSheetDialogFragment
import kr.co.hs.sudoku.feature.multi.MultiPlayCreateActivity
import kr.co.hs.sudoku.feature.profile.ProfileBottomSheetDialog
import kr.co.hs.sudoku.feature.profile.ProfilePopupMenu
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import javax.inject.Inject

@AndroidEntryPoint
class ChallengeDashboardFragment : Fragment(), ProfilePopupMenu.OnPopupMenuItemClickListener {
    companion object {
        fun newInstance() = ChallengeDashboardFragment()
    }

    private lateinit var binding: LayoutListChallengeBinding
    private val viewModel: ChallengeDashboardViewModel by activityViewModels()

    @Inject
    @ChallengeRepositoryQualifier
    lateinit var challengeRepository: ChallengeRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutListChallengeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.swipeRefreshLayout) {
            setOnRefreshListener { pagingDataAdapter.refresh() }
        }

        pagingDataAdapter.apply {
            addLoadStateListener { loadState ->
                when (loadState.refresh) {
                    is LoadState.NotLoading, is LoadState.Error -> {
                        binding.swipeRefreshLayout.isRefreshing = false
                        dismissProgressIndicator()
                    }

                    LoadState.Loading -> {
                        if (!binding.swipeRefreshLayout.isRefreshing) {
                            showProgressIndicator()
                        }
                    }
                }

                if (loadState.refresh is LoadState.NotLoading && loadState.append is LoadState.NotLoading) {
                    // 페이지 로드 완료 후 동작
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        with(binding.recyclerViewChallengeList) {
            layoutManager = LinearLayoutManager(context)
            addVerticalDivider(thickness = 40.dp)
            adapter = pagingDataAdapter.withLoadStateFooter(PagingLoadStateAdapter())
        }

        viewModel.challengeDashboardPagingData.observe(viewLifecycleOwner) {
            viewLifecycleOwner.lifecycleScope.launch { pagingDataAdapter.submitData(it) }
        }
    }

    private val pagingDataAdapter: ChallengeDashboardListItemAdapter
            by lazy {
                ChallengeDashboardListItemAdapter(
                    challengeRepository,
                    onClickLeaderBoard = { showLeaderBoard(it) },
                    onClickStart = { startChallenge(it) },
                    this
                )
            }

    private fun showLeaderBoard(challengeId: String) = viewLifecycleOwner.lifecycleScope.launch {
        LeaderBoardBottomSheetDialogFragment
            .showChallengeLeaderBoard(childFragmentManager, challengeId)
    }

    private val launcherForChallengePlay =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val challengeId = it.data?.getStringExtra(ChallengePlayActivity.EXTRA_CHALLENGE_ID)
                    ?: return@registerForActivityResult
                refreshChallenge(challengeId)
            }
        }

    private fun refreshChallenge(id: String) = with(binding.recyclerViewChallengeList) {
        with(pagingDataAdapter) {
            snapshot()
                .indexOfFirst { item -> item?.id == id }
                .takeIf { idx -> idx >= 0 }
                ?.let { idx ->
                    notifyItemChanged(idx)
                    smoothScrollToPosition(idx)
                }
        }
    }

    private fun startChallenge(challengeEntity: ChallengeEntity) =
        viewLifecycleOwner.lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            when (throwable) {
                is AlreadyException -> showConfirm(
                    getString(R.string.app_name),
                    throwable.message.toString()
                ) {
                    if (it) {
                        retryChallenge(challengeEntity.challengeId)
                    }
                }

                else -> showAlert(
                    getString(R.string.app_name),
                    throwable.message.toString()
                ) {}
            }
        }) {
            if (challengeEntity.isComplete)
                throw AlreadyException(getString(R.string.error_challenge_already_record))
            launcherForChallengePlay.launch(
                ChallengePlayActivity.newIntent(
                    requireContext(),
                    challengeEntity.challengeId
                )
            )
        }

    private class AlreadyException(message: String?) : Exception(message)


    private fun retryChallenge(challengeId: String) {
        viewLifecycleOwner.lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            dismissProgressIndicator()
            showAlert(getString(R.string.app_name), throwable.message.toString()) {}
        }) {
            showProgressIndicator()
            val result = ChallengeRetryRewardAdManager(requireActivity()).showRewardedAd()
            if (!result)
                throw InvalidRewardedException(getString(R.string.error_challenge_retry_rewarded))

            viewModel.doDeleteRecord(challengeId)
            dismissProgressIndicator()

            ChallengePlayActivity.start(requireContext(), challengeId)
        }
    }

    private class InvalidRewardedException(message: String?) : Exception(message)

    override fun onClickProfile(uid: String) =
        ProfileBottomSheetDialog.show(childFragmentManager, uid)

    override fun onClickInviteMultiPlay(uid: String, displayName: String) {
        val title = getString(R.string.multi_play_invite_confirm_title)
        val message = getString(R.string.multi_play_invite_confirm_message, displayName)
        (requireActivity() as Activity).showConfirm(title, message) {
            if (it) {
                MultiPlayCreateActivity.start(requireActivity(), uid)
            }
        }
    }

    fun refreshPagingData() = pagingDataAdapter.refresh()
}