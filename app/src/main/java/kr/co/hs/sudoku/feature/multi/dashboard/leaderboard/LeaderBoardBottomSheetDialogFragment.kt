package kr.co.hs.sudoku.feature.multi.dashboard.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.LayoutLeaderboardMultiPlayBinding
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.feature.multi.MultiPlayCreateActivity
import kr.co.hs.sudoku.feature.profile.ProfileBottomSheetDialog
import kr.co.hs.sudoku.feature.profile.UserProfileViewModel
import kr.co.hs.sudoku.feature.multi.dashboard.MultiDashboardViewModel
import kr.co.hs.sudoku.feature.profile.ProfilePopupMenu
import kr.co.hs.sudoku.model.battle.BattleLeaderBoardEntity

class LeaderBoardBottomSheetDialogFragment : BottomSheetDialogFragment(),
    ProfilePopupMenu.OnPopupMenuItemClickListener {
    companion object {
        fun show(fragmentManager: FragmentManager) {
            LeaderBoardBottomSheetDialogFragment().show(
                fragmentManager,
                LeaderBoardBottomSheetDialogFragment::class.java.name
            )
        }
    }

    private lateinit var binding: LayoutLeaderboardMultiPlayBinding
    private val viewModel: MultiDashboardViewModel by activityViewModels()
    private val profileViewModel: UserProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutLeaderboardMultiPlayBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerViewList) {
            this.layoutManager = LinearLayoutManager(context)
            addVerticalDivider(10.dp)
            val itemAdapter =
                LeaderBoardListAdapter(profileViewModel, this@LeaderBoardBottomSheetDialogFragment)

            itemAdapter.submitList(
                List(10) {
                    LeaderBoardItem.ListItem(
                        BattleLeaderBoardEntity("", 0, 0, it.toLong() + 1),
                        false
                    )
                }
            )
            this.adapter = itemAdapter
            viewModel.leaderBoard.observe(viewLifecycleOwner) { itemAdapter.submitList(it) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { viewModel.requestLeaderBoard(10) }
        }
    }

    override fun onClickProfile(uid: String) =
        ProfileBottomSheetDialog.show(childFragmentManager, uid)

    override fun onClickInviteMultiPlay(uid: String, displayName: String) {
        val title = getString(R.string.multi_play_invite_confirm_title)
        val message = getString(R.string.multi_play_invite_confirm_message, displayName)
        (requireActivity() as Activity).showConfirm(title, message) {
            if (it) {
                dismiss()
                MultiPlayCreateActivity.start(requireActivity(), uid)
            }
        }
    }
}