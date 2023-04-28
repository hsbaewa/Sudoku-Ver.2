package kr.co.hs.sudoku.feature.challenge

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.viewmodel.ChallengeViewModel

class ChallengeLeaderboardFragment : Fragment() {

    companion object {
        fun new(uid: String?) = ChallengeLeaderboardFragment()
            .apply {
                uid?.let {
                    arguments = Bundle().apply { putUserId(it) }
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = LayoutChallengeLeaderboardBinding.inflate(inflater, container, false).also {
        binding = it
        it.lifecycleOwner = this
    }.root

    lateinit var binding: LayoutChallengeLeaderboardBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (getUserId() == null)
            showSnackBar(getString(R.string.error_require_authenticate))

        challengeViewModel.let {
            it.challenge.observe(viewLifecycleOwner, onChallenge)
            it.top10.observe(viewLifecycleOwner, onChangedTop10)
            it.myRecord.observe(viewLifecycleOwner, onMyRecord)
            it.error.observe(viewLifecycleOwner, onError)
            it.isRunningProgress.observe(viewLifecycleOwner, onProgress)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { requestChallenge() }
        }

        refreshLayout.setOnRefreshListener {
            app.clearChallengeRecordRepository()
            app.clearChallengeRepository()
            requestChallenge()
        }
    }

    private val challengeViewModel: ChallengeViewModel by lazy { challengeLeaderboardViewModels() }

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- 도전 정보 -------------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private val onChallenge = Observer<ChallengeEntity> {
        challengeSudokuBoard.setRowCount(it.matrix.rowCount, it.matrix)

        val challengeId = it.challengeId
        val uid = getUserId()

        if (challengeId != null && uid != null) {
            startButton.visibility = View.VISIBLE
            startButton.setOnClickListener { _ ->
                if (it.isComplete) {
                    showSnackBar(getString(R.string.error_challenge_already_record))
                } else {
                    activity.startChallengePlayActivity(challengeId, uid)
                }
            }
            requestLeaderboard(challengeId, uid)
        }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/26
     * @comment 도전할 스도쿠 보드가 표시될 뷰
     **/
    private val challengeSudokuBoard by lazy {
        binding.sudokuBoard.apply { visibility = View.VISIBLE }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/26
     * @comment 스도쿠 시작 버튼
     **/
    private val startButton by lazy {
        binding.btnStart.apply { setAutoSizeText() }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/26
     * @comment 리더보드 요청
     * @param challengeId
     * @param uid
     **/
    private fun requestLeaderboard(challengeId: String, uid: String) {
        challengeViewModel.requestLeaderboard(
            app.getChallengeRecordRepository(challengeId), uid
        )
    }

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- 리더보드 top10 ----------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private val onChangedTop10 = Observer<List<RankerEntity>> {
        leaderBoardCardView.isVisible = it.isNotEmpty()
        rankingAdapter.submitList(it) {
            leaderBoardView.smoothScrollToPosition(0)
        }
    }

    private val rankingAdapter by lazy { RankingAdapter(onRankerItemClick) }
    private val onRankerItemClick = { _: Int, item: RankerEntity ->
        context?.run { item.showDialog(this) } ?: Unit
    }

    private fun ProfileEntity.showDialog(context: Context) =
        takeIf { uid.isNotEmpty() }
            ?.run { ProfileDialog(context, this) }
            ?.show()

    private val leaderBoardView by lazy {
        binding.recyclerViewLeaderBoard.apply {
            adapter = rankingAdapter
        }
    }

    private val leaderBoardCardView by lazy { binding.cardLeaderBoard }

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- 내 기록 표시 ------------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private val onMyRecord = Observer<RankerEntity?> {
        it?.run {
            RankForMineViewHolder(binding.layoutMyRank).onBind(this)
            binding.cardMyRecord.visibility = View.VISIBLE
        } ?: kotlin.run { binding.cardMyRecord.visibility = View.GONE }
    }


    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- 에러 핸들 -------------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private val onError = Observer<Throwable> { showSnackBar(it.message.toString()) }


    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- Progress 표시 -----------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private val onProgress = Observer<Boolean> {
        if (it) {
            showProgressIndicator()
        } else {
            dismissProgressIndicator()
            refreshLayout.isRefreshing = false
        }
    }


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/25
     * @comment ViewModel에 Challenge 정보 요청
     **/
    private fun requestChallenge() =
        challengeViewModel.requestLatestChallenge(app.getChallengeRepository())

    private val refreshLayout by lazy {
        binding.swipeRefreshLayout.apply {
            setColorSchemeColors(getColorCompat(R.color.gray_500))
        }
    }
}