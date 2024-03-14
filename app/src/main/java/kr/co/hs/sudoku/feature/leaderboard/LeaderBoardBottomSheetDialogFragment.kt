package kr.co.hs.sudoku.feature.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.LayoutLeaderboardMultiPlayBinding
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.feature.multi.MultiPlayCreateActivity
import kr.co.hs.sudoku.feature.profile.ProfileBottomSheetDialog
import kr.co.hs.sudoku.feature.profile.ProfilePopupMenu
import kr.co.hs.sudoku.repository.user.ProfileRepository
import javax.inject.Inject


@AndroidEntryPoint
class LeaderBoardBottomSheetDialogFragment : BottomSheetDialogFragment(),
    ProfilePopupMenu.OnPopupMenuItemClickListener {
    companion object {
        private const val EXTRA_TYPE = "kr.co.hs.sudoku.feature.leaderboard.EXTRA_TYPE"
        private const val TYPE_BATTLE = "TYPE_BATTLE"
        private const val TYPE_CHALLENGE = "TYPE_CHALLENGE"
        private const val EXTRA_ID = "kr.co.hs.sudoku.feature.leaderboard.EXTRA_ID"

        fun showBattleLeaderBoard(fragmentManager: FragmentManager) =
            LeaderBoardBottomSheetDialogFragment()
                .apply { arguments = bundleOf(EXTRA_TYPE to TYPE_BATTLE) }
                .show(fragmentManager, LeaderBoardBottomSheetDialogFragment::class.java.name)

        fun showChallengeLeaderBoard(fragmentManager: FragmentManager, challengeId: String) =
            LeaderBoardBottomSheetDialogFragment()
                .apply {
                    arguments = bundleOf(
                        EXTRA_TYPE to TYPE_CHALLENGE,
                        EXTRA_ID to challengeId
                    )
                }
                .show(fragmentManager, LeaderBoardBottomSheetDialogFragment::class.java.name)
    }

    private lateinit var binding: LayoutLeaderboardMultiPlayBinding
    private val app: App by lazy { requireContext().applicationContext as App }
    private val viewModel: LeaderBoardListViewModel by viewModels()

    @Inject
    @kr.co.hs.sudoku.di.ProfileRepositoryQualifier
    lateinit var profileRepository: ProfileRepository

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
            val itemAdapter = LeaderBoardListAdapter(
                profileRepository,
                this@LeaderBoardBottomSheetDialogFragment
            )
            this.adapter = itemAdapter
            viewModel.leaderBoardList.observe(viewLifecycleOwner) { itemAdapter.submitList(it) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                when (arguments?.getString(EXTRA_TYPE)) {
                    TYPE_BATTLE -> viewModel.requestBattleLeaderBoard()
                    TYPE_CHALLENGE ->
                        viewModel.requestChallengeLeaderBoard(arguments?.getString(EXTRA_ID) ?: "")

                    else -> {}
                }
            }
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