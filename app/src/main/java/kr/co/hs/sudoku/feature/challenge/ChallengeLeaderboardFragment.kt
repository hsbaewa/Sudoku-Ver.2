package kr.co.hs.sudoku.feature.challenge

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutChallengeLeaderboardBinding
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showSnackBar
import kr.co.hs.sudoku.feature.challenge.ChallengePlayActivity.Companion.startChallengePlayActivity
import kr.co.hs.sudoku.feature.profile.ProfileDialog
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.viewmodel.ChallengeViewModel

class ChallengeLeaderboardFragment : Fragment() {

    companion object {
        fun new() = ChallengeLeaderboardFragment()
    }

    lateinit var binding: LayoutChallengeLeaderboardBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = LayoutChallengeLeaderboardBinding.inflate(inflater, container, false).also {
        binding = it
        it.lifecycleOwner = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            recyclerViewLeaderBoard.onCreatedLeaderBoard()
            swipeRefreshLayout.onCreatedRefreshLayout()
            btnStart.onCreatedStartButton()
        }

        challengeViewModel.initObserver()

        viewLifecycleOwner.lifecycleScope.launch {
            withStarted { refreshLeaderBoard() }
        }
    }

    private val currentUser: FirebaseUser?
        get() = FirebaseAuth.getInstance().currentUser

    private val challengeViewModel: ChallengeViewModel by viewModels()


    private fun RecyclerView.onCreatedLeaderBoard() {
        adapter = RankingAdapter(onRankerItemClick)
    }

    private val onRankerItemClick = { _: Int, item: RankerEntity ->
        context?.run { item.showDialog(this) } ?: Unit
    }

    private fun ProfileEntity.showDialog(context: Context) =
        takeIf { uid.isNotEmpty() }
            ?.run { ProfileDialog(context, this) }
            ?.show()

    private fun Button.onCreatedStartButton() {
        setOnClickListener {
            challengeViewModel.challenge.value?.startChallenge()
        }
    }

    private fun ChallengeEntity.startChallenge() =
        viewLifecycleOwner.lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            showSnackBar(throwable.message.toString())
        }) {
            if (isComplete)
                throw Exception(getString(R.string.error_challenge_already_record))
            val uid =
                currentUser?.uid ?: throw Exception(getString(R.string.error_require_authenticate))
            startChallengePlayActivity(uid, challengeId)
        }

    private fun startChallengePlayActivity(uid: String, challengeId: String) =
        viewLifecycleOwner.lifecycleScope.launch {
            activity.startChallengePlayActivity(challengeId, uid)
        }

    private fun SwipeRefreshLayout.onCreatedRefreshLayout() {
        setOnRefreshListener {
            app.clearChallengeRepository()
            refreshLeaderBoard()
        }
    }

    private fun refreshLeaderBoard() = currentUser
        ?.run { challengeViewModel.requestLeaderBoard(uid, app.getChallengeRepository()) }
        ?: viewLifecycleOwner.lifecycleScope.launch {
            showSnackBar(getString(R.string.error_require_authenticate))
        }

    private fun ChallengeViewModel.initObserver() {
        challenge.observe(viewLifecycleOwner) {
            setupUIPreview(it)
            setupUIStartButton(it)
        }
        top10.observe(viewLifecycleOwner) { setupUITop10List(it) }
        myRecord.observe(viewLifecycleOwner) { setupUIMyRank(it) }

        isRunningProgress.observe(viewLifecycleOwner) { setupUIOnProgress(it) }
        error.observe(viewLifecycleOwner) {
            viewLifecycleOwner.lifecycleScope.launch { showSnackBar(it.message.toString()) }
        }
    }

    private fun setupUIPreview(entity: ChallengeEntity) {
        with(binding.sudokuBoard) {
            isVisible = true
            setRowCount(entity.matrix.rowCount, entity.matrix)
        }
    }

    private fun setupUIStartButton(entity: ChallengeEntity?) {
        with(binding) {
            btnStart.isVisible = entity != null
        }
    }

    private fun setupUITop10List(list: List<RankerEntity>) {
        with(binding) {
            cardLeaderBoard.isVisible = list.isNotEmpty()

            with(recyclerViewLeaderBoard) {
                (adapter as? RankingAdapter)?.run {
                    submitList(list) { smoothScrollToPosition(0) }
                }
            }

        }
    }

    private fun setupUIMyRank(entity: RankerEntity?) {
        with(binding) {
            cardMyRecord.isVisible = entity != null
            entity?.run { RankForMineViewHolder(layoutMyRank).onBind(this) }
        }
    }

    private fun setupUIOnProgress(isProgress: Boolean) {
        if (isProgress) {
            if (!binding.swipeRefreshLayout.isRefreshing) {
                showProgressIndicator()
            }
        } else {
            with(binding.swipeRefreshLayout) {
                if (isRefreshing) {
                    isRefreshing = false
                }
            }
            dismissProgressIndicator()
        }
    }
}