package kr.co.hs.sudoku.feature.challenge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutChallengeLeaderboardBinding
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showSnackBar
import kr.co.hs.sudoku.extension.platform.TextViewExtension.setAutoSizeText
import kr.co.hs.sudoku.feature.profile.ProfileDialog

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

    lateinit var binding: LayoutChallengeLeaderboardBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.run {
            recyclerViewLeaderBoard.setupUILeaderBoard()
            btnStart.setupUIStart()

            challengeRankingViewModels().run {
                error.observe(viewLifecycleOwner) { showSnackBar(it.message.toString()) }
                isRunningProgress.observe(viewLifecycleOwner) {
                    if (it) {
                        showProgressIndicator()
                    } else {
                        dismissProgressIndicator()
                    }
                }
                top10.observe(viewLifecycleOwner) {
                    val adapter = recyclerViewLeaderBoard.adapter as RankingAdapter
                    adapter.submitList(it)
                }
                myRecord.observe(viewLifecycleOwner) {
                    if (it != null) {
                        cardMyRecord.visibility = View.VISIBLE
                        RankForMineViewHolder(layoutMyRank).onBind(it)
                    } else {
                        cardMyRecord.visibility = View.GONE
                    }
                }
            }
        }

    }

    private fun RecyclerView.setupUILeaderBoard() {
        val rankingAdapter = RankingAdapter {
            val profile = (adapter as RankingAdapter).currentList[it]
            ProfileDialog(context, profile).show()
        }
        adapter = rankingAdapter
    }

    private fun MaterialButton.setupUIStart() {
        setAutoSizeText()
    }
}