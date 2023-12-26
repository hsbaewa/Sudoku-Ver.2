package kr.co.hs.sudoku.feature.challenge2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutListChallengeRankBinding
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.platform.ContextExtension.getColorCompat
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showSnackBar
import kr.co.hs.sudoku.feature.challenge.ChallengePlayActivity.Companion.startChallengePlayActivity
import kr.co.hs.sudoku.model.challenge.ChallengeEntity

class ChallengeDashboardFragment : Fragment() {
    companion object {
        fun newInstance() = ChallengeDashboardFragment()
    }

    private lateinit var binding: LayoutListChallengeRankBinding
    private val viewModel: ChallengeDashboardViewModel by activityViewModels()

    private val currentUserUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw Exception("사용자 인증 정보가 없습니다. 게임 진행을 위해서는 먼저 사용자 인증이 필요합니다.")

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
        viewModel.isRunningProgress.observe(viewLifecycleOwner) {
            if (!it && binding.swipeRefreshLayout.isRefreshing) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        with(binding.swipeRefreshLayout) {
            setColorSchemeColors(context.getColorCompat(R.color.gray_500))
            setOnRefreshListener { viewModel.requestChallengeDashboard() }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { viewModel.requestChallengeDashboard() }
        }

    }

    private fun List<ChallengeDashboardListItem>.sorted() = sortedWith { item1, item2 ->
        return@sortedWith if (item1 is ChallengeDashboardListItem.RankItem && item2 is ChallengeDashboardListItem.RankItem) {
            val rank1 = item1.rankEntity.rank
            val rank2 = item2.rankEntity.rank
            rank1.compareTo(rank2)
        } else {
            item1.order.compareTo(item2.order)
        }
    }

    private fun startChallenge(challengeEntity: ChallengeEntity) =
        viewLifecycleOwner.lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            showSnackBar(throwable.message.toString())
        }) {
            if (challengeEntity.isComplete)
                throw Exception(getString(R.string.error_challenge_already_record))
            activity.startChallengePlayActivity(challengeEntity.challengeId, currentUserUid)
        }
}