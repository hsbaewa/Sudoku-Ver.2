package kr.co.hs.sudoku.feature.challenge

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutChallengeLeaderboardBinding
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showSnackBar
import kr.co.hs.sudoku.extension.platform.TextViewExtension.setAutoSizeText
import kr.co.hs.sudoku.feature.challenge.ChallengePlayActivity.Companion.startChallengePlayActivity
import kr.co.hs.sudoku.feature.profile.ProfileDialog
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.challenge.ChallengeReaderRepositoryImpl
import kr.co.hs.sudoku.repository.record.ChallengeRecordRepository
import kr.co.hs.sudoku.viewmodel.ChallengeViewModel
import kr.co.hs.sudoku.views.SudokuBoardView

class ChallengeLeaderboardFragment : Fragment() {

    companion object {
        fun new() = ChallengeLeaderboardFragment()
    }

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

        binding.recyclerViewLeaderBoard.setupUILeaderBoard()

        leaderboardViewModel.let {
            it.error.observe(viewLifecycleOwner) { t -> onError(t) }
            it.isRunningProgress.observe(viewLifecycleOwner) { b -> onChangedRunningProgressState(b) }
            it.top10.observe(viewLifecycleOwner) { list ->
                binding.recyclerViewLeaderBoard.onChangedTop10(list)
            }
            it.myRecord.observe(viewLifecycleOwner) { entity -> setupUIMyRecord(entity) }
            it.challenge.observe(viewLifecycleOwner) { entity ->
                binding.sudokuBoard.setupUI(entity.matrix)
                entity.challengeId?.run {
                    binding.btnStart.setupUIStart(this)
                    leaderboardViewModel.requestLeaderboard(
                        ChallengeRecordRepository(this),
                        FirebaseAuth.getInstance().currentUser?.uid
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                leaderboardViewModel.requestLatestChallenge(ChallengeReaderRepositoryImpl())
            }
        }
    }

    lateinit var binding: LayoutChallengeLeaderboardBinding

    private fun RecyclerView.setupUILeaderBoard() {
        adapter = RankingAdapter {
            (adapter as RankingAdapter)
                .currentList[it]
                .showDialog(context)
        }
    }

    private fun ProfileEntity.showDialog(context: Context) =
        takeIf { uid.isNotEmpty() }
            ?.run { ProfileDialog(context, this) }
            ?.show()

    private fun MaterialButton.setupUIStart(challengeId: String) {
        setAutoSizeText()
        FirebaseAuth.getInstance().currentUser?.uid.takeIf { it != null }
            ?.run {
                visibility = View.VISIBLE
                setOnClickListener {
                    activity.startChallengePlayActivity(challengeId, this)
                }
            }
            ?: showSnackBar(getString(R.string.error_require_authenticate))
    }

    private val leaderboardViewModel: ChallengeViewModel by lazy { challengeLeaderboardViewModels() }

    private fun onError(t: Throwable) = showSnackBar(t.message.toString())
    private fun onChangedRunningProgressState(progress: Boolean) = if (progress) {
        showProgressIndicator()
    } else {
        dismissProgressIndicator()
    }

    private fun RecyclerView.onChangedTop10(list: List<RankerEntity>) {
        with(adapter as RankingAdapter) {
            submitList(list) { smoothScrollToPosition(0) }
        }
    }

    private fun setupUIMyRecord(rankerEntity: RankerEntity?) =
        rankerEntity.takeIf { it != null }
            ?.run { RankForMineViewHolder(binding.layoutMyRank).onBind(this) }
            ?.run { binding.cardMyRecord.visibility = View.VISIBLE }
            ?: kotlin.run { binding.cardMyRecord.visibility = View.GONE }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment 스도쿠 스테이지 ui setup
     * @param stage 선택된
     **/
    private fun SudokuBoardView.setupUI(stage: IntMatrix?) = stage?.let {
        isVisible = true
        setRowCount(it.rowCount, stage)
    }
}