package kr.co.hs.sudoku.feature.challenge.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutListChallengeRankBinding
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.platform.FragmentExtension.isShowProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showSnackBar
import kr.co.hs.sudoku.feature.challenge.play.ChallengePlayActivity
import kr.co.hs.sudoku.model.challenge.ChallengeEntity

class ChallengeDashboardFragment : Fragment() {
    companion object {
        fun newInstance() = ChallengeDashboardFragment()
    }

    private lateinit var binding: LayoutListChallengeRankBinding
    private val viewModel: ChallengeDashboardViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutListChallengeRankBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerViewRankList) {
            layoutManager = LinearLayoutManager(context)
            addVerticalDivider(thickness = 10.dp)
            adapter = ChallengeDashboardListItemAdapter { startChallenge(it) }
        }

        with(binding.recyclerViewRankList.adapter as ChallengeDashboardListItemAdapter) {
            submitList(listOf(ChallengeDashboardListItem.TitleItem))
        }

        viewModel.dashboardItemList.observe(viewLifecycleOwner) {
            with(binding.recyclerViewRankList.adapter as ChallengeDashboardListItemAdapter) {
                submitList(it)
            }
        }


        with(binding.swipeRefreshLayout) {
            setOnRefreshListener { viewModel.requestChallengeDashboard() }
            viewModel.isRunningProgress.observe(viewLifecycleOwner) {
                if (isRefreshing) {
                    if (!it) {
                        binding.swipeRefreshLayout.isRefreshing = it
                    }
                } else {
                    isShowProgressIndicator = it
                }

            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { viewModel.requestChallengeDashboard() }
        }

    }

    private fun startChallenge(challengeEntity: ChallengeEntity) =
        viewLifecycleOwner.lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            showSnackBar(throwable.message.toString())
        }) {
            if (challengeEntity.isComplete)
                throw Exception(getString(R.string.error_challenge_already_record))
            ChallengePlayActivity.start(requireContext(), challengeEntity.challengeId)
        }
}